package com.jantabank.dto.profile;

import com.jantabank.domain.enums.KycDocumentType;
import com.jantabank.domain.enums.KycStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class KycDocumentResponse {
    private Long id;
    private KycDocumentType documentType;
    private String maskedDocumentNumber;
    private String contentType;
    private long fileSize;
    private KycStatus status;
    private String remarks;
    private LocalDateTime uploadedAt;
    private LocalDateTime verifiedAt;
}
