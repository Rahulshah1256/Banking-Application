package com.jantabank.impl;

import com.jantabank.domain.enums.KycStatus;
import com.jantabank.dto.profile.ProfileResponse;
import com.jantabank.dto.profile.StoredFileContent;
import com.jantabank.dto.profile.UpdateProfileRequest;
import com.jantabank.entity.CustomerProfile;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.CustomerProfileRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.service.FileStorageService;
import com.jantabank.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final UserRepository userRepository;
    private final CustomerProfileRepository profileRepository;
    private final FileStorageService fileStorageService;

    public ProfileServiceImpl(UserRepository userRepository,
                              CustomerProfileRepository profileRepository,
                              FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public ProfileResponse getProfile(String username) {
        User user = loadUser(username);
        CustomerProfile profile = getOrCreateProfile(user);
        return toResponse(user, profile);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(UpdateProfileRequest request, String username) {
        User user = loadUser(username);
        CustomerProfile profile = getOrCreateProfile(user);

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            user.setAddress(request.getAddress().trim());
            userRepository.save(user);
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getOccupation() != null) {
            profile.setOccupation(request.getOccupation().trim());
        }
        if (request.getAnnualIncome() != null) {
            profile.setAnnualIncome(request.getAnnualIncome());
        }
        if (request.getCommunicationAddress() != null) {
            profile.setCommunicationAddress(request.getCommunicationAddress().trim());
        }
        if (request.getPermanentAddress() != null) {
            profile.setPermanentAddress(request.getPermanentAddress().trim());
        }
        if (request.getAlternatePhone() != null) {
            profile.setAlternatePhone(request.getAlternatePhone().trim());
        }
        profileRepository.save(profile);
        log.info("Profile updated for user {}", user.getId());
        return toResponse(user, profile);
    }

    @Override
    @Transactional
    public ProfileResponse uploadPhoto(byte[] content, String contentType, String originalFilename, String username) {
        User user = loadUser(username);
        CustomerProfile profile = getOrCreateProfile(user);
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Profile photo must be an image");
        }
        String oldPath = profile.getProfilePhotoPath();
        String path = fileStorageService.store(content, "profile-photos/" + user.getId(), originalFilename);
        profile.setProfilePhotoPath(path);
        profile.setProfilePhotoContentType(contentType);
        profileRepository.save(profile);
        if (oldPath != null && !oldPath.equals(path)) {
            fileStorageService.delete(oldPath);
        }
        log.info("Profile photo updated for user {}", user.getId());
        return toResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public StoredFileContent getPhoto(String username) {
        User user = loadUser(username);
        CustomerProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile photo not found"));
        if (profile.getProfilePhotoPath() == null) {
            throw new ResourceNotFoundException("Profile photo not found");
        }
        byte[] bytes = fileStorageService.load(profile.getProfilePhotoPath());
        String ct = profile.getProfilePhotoContentType() != null
                ? profile.getProfilePhotoContentType() : "application/octet-stream";
        return new StoredFileContent(bytes, ct);
    }

    private CustomerProfile getOrCreateProfile(User user) {
        return profileRepository.findByUserId(user.getId()).orElseGet(() -> {
            CustomerProfile p = new CustomerProfile();
            p.setUserId(user.getId());
            p.setKycStatus(KycStatus.PENDING);
            return profileRepository.save(p);
        });
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProfileResponse toResponse(User user, CustomerProfile profile) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .mobile(user.getMobile())
                .maskedPan(maskTail(user.getPanNo(), 4))
                .maskedAadhaar(maskTail(user.getAadhaarNo(), 4))
                .address(user.getAddress())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .occupation(profile.getOccupation())
                .annualIncome(profile.getAnnualIncome())
                .communicationAddress(profile.getCommunicationAddress())
                .permanentAddress(profile.getPermanentAddress())
                .alternatePhone(profile.getAlternatePhone())
                .hasProfilePhoto(profile.getProfilePhotoPath() != null)
                .kycStatus(profile.getKycStatus())
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
