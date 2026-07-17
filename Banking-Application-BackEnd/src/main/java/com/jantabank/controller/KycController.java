package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.domain.enums.KycDocumentType;
import com.jantabank.dto.profile.KycDocumentResponse;
import com.jantabank.dto.profile.KycStatusResponse;
import com.jantabank.dto.profile.StoredFileContent;
import com.jantabank.dto.profile.VerifyKycRequest;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.service.KycService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("api/kyc")
@AllArgsConstructor
public class KycController {

    private final KycService kycService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycDocumentResponse>> upload(
            @RequestParam("type") KycDocumentType type,
            @RequestParam(value = "documentNumber", required = false) String documentNumber,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        byte[] bytes = readBytes(file);
        KycDocumentResponse response = kycService.upload(type, documentNumber, bytes,
                file.getContentType(), file.getOriginalFilename(), principal.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Document uploaded"), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> list(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(kycService.listMine(principal.getName()), "Documents retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<KycStatusResponse>> status(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(kycService.status(principal.getName()), "KYC status retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/documents/{id}/file")
    public ResponseEntity<byte[]> download(@PathVariable Long id, Principal principal) {
        StoredFileContent content = kycService.download(id, principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.getContentType()))
                .body(content.getContent());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/documents/{id}/verify")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> verify(@PathVariable Long id,
                                                                   @Valid @RequestBody VerifyKycRequest request) {
        return ResponseEntity.ok(ApiResponse.success(kycService.verify(id, request), "Document decision recorded"));
    }

    private byte[] readBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "File is required");
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Could not read uploaded file");
        }
    }
}
