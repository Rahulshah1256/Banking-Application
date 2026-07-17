package com.jantabank.impl;

import com.jantabank.domain.enums.KycDocumentType;
import com.jantabank.domain.enums.KycStatus;
import com.jantabank.dto.profile.KycDocumentResponse;
import com.jantabank.dto.profile.KycStatusResponse;
import com.jantabank.dto.profile.StoredFileContent;
import com.jantabank.dto.profile.VerifyKycRequest;
import com.jantabank.entity.CustomerProfile;
import com.jantabank.entity.KycDocument;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.CustomerProfileRepository;
import com.jantabank.repository.KycDocumentRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.FileStorageService;
import com.jantabank.service.KycService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KycServiceImpl implements KycService {

    private static final Logger log = LoggerFactory.getLogger(KycServiceImpl.class);

    private final KycDocumentRepository kycDocumentRepository;
    private final CustomerProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public KycServiceImpl(KycDocumentRepository kycDocumentRepository,
                          CustomerProfileRepository profileRepository,
                          UserRepository userRepository,
                          FileStorageService fileStorageService) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public KycDocumentResponse upload(KycDocumentType type, String documentNumber, byte[] content,
                                      String contentType, String originalFilename, String username) {
        User user = loadUser(username);
        if (type == null) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Document type is required");
        }
        String path = fileStorageService.store(content, "kyc/" + user.getId(), originalFilename);

        KycDocument doc = new KycDocument();
        doc.setUserId(user.getId());
        doc.setDocumentType(type);
        doc.setDocumentNumber(documentNumber != null ? documentNumber.trim() : null);
        doc.setStoragePath(path);
        doc.setContentType(contentType);
        doc.setFileSize(content.length);
        doc.setStatus(KycStatus.PENDING);
        doc.setUploadedAt(LocalDateTime.now());
        doc = kycDocumentRepository.save(doc);

        syncProfileStatus(user);
        log.info("KYC document {} ({}) uploaded for user {}", doc.getId(), type, user.getId());
        return toResponse(doc);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycDocumentResponse> listMine(String username) {
        User user = loadUser(username);
        return kycDocumentRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KycStatusResponse status(String username) {
        User user = loadUser(username);
        List<KycDocument> docs = kycDocumentRepository.findByUserIdOrderByIdDesc(user.getId());
        long verified = docs.stream().filter(d -> d.getStatus() == KycStatus.VERIFIED).count();
        long pending = docs.stream().filter(d -> d.getStatus() == KycStatus.PENDING).count();
        long rejected = docs.stream().filter(d -> d.getStatus() == KycStatus.REJECTED).count();
        return KycStatusResponse.builder()
                .overallStatus(deriveOverall(docs))
                .totalDocuments(docs.size())
                .verifiedDocuments(verified)
                .pendingDocuments(pending)
                .rejectedDocuments(rejected)
                .documents(docs.stream().map(this::toResponse).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StoredFileContent download(Long documentId, String username) {
        User user = loadUser(username);
        KycDocument doc = kycDocumentRepository.findByIdAndUserId(documentId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));
        byte[] bytes = fileStorageService.load(doc.getStoragePath());
        String ct = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";
        return new StoredFileContent(bytes, ct);
    }

    @Override
    @Transactional
    public KycDocumentResponse verify(Long documentId, VerifyKycRequest request) {
        if (request.getStatus() != KycStatus.VERIFIED && request.getStatus() != KycStatus.REJECTED) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Decision must be VERIFIED or REJECTED");
        }
        KycDocument doc = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));
        doc.setStatus(request.getStatus());
        doc.setRemarks(request.getRemarks());
        doc.setVerifiedAt(LocalDateTime.now());
        kycDocumentRepository.save(doc);

        userRepository.findById(doc.getUserId()).ifPresent(this::syncProfileStatus);
        log.info("KYC document {} marked {}", documentId, request.getStatus());
        return toResponse(doc);
    }

    private void syncProfileStatus(User user) {
        List<KycDocument> docs = kycDocumentRepository.findByUserIdOrderByIdDesc(user.getId());
        KycStatus overall = deriveOverall(docs);
        CustomerProfile profile = profileRepository.findByUserId(user.getId()).orElseGet(() -> {
            CustomerProfile p = new CustomerProfile();
            p.setUserId(user.getId());
            return p;
        });
        profile.setKycStatus(overall);
        profileRepository.save(profile);
    }

    private KycStatus deriveOverall(List<KycDocument> docs) {
        if (docs.isEmpty()) {
            return KycStatus.PENDING;
        }
        boolean allVerified = docs.stream().allMatch(d -> d.getStatus() == KycStatus.VERIFIED);
        if (allVerified) {
            return KycStatus.VERIFIED;
        }
        boolean anyPending = docs.stream().anyMatch(d -> d.getStatus() == KycStatus.PENDING);
        if (anyPending) {
            return KycStatus.PENDING;
        }
        return KycStatus.REJECTED;
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private KycDocumentResponse toResponse(KycDocument d) {
        return KycDocumentResponse.builder()
                .id(d.getId())
                .documentType(d.getDocumentType())
                .maskedDocumentNumber(maskTail(d.getDocumentNumber(), 4))
                .contentType(d.getContentType())
                .fileSize(d.getFileSize())
                .status(d.getStatus())
                .remarks(d.getRemarks())
                .uploadedAt(d.getUploadedAt())
                .verifiedAt(d.getVerifiedAt())
                .build();
    }

    private String maskTail(String value, int visible) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        if (v.length() <= visible) {
            return "X".repeat(Math.max(0, v.length()));
        }
        return "X".repeat(v.length() - visible) + v.substring(v.length() - visible);
    }
}
