package com.jantabank.entity;

import com.jantabank.domain.enums.KycDocumentType;
import com.jantabank.domain.enums.KycStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "kyc_documents")
public class KycDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 30, nullable = false)
    private KycDocumentType documentType;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Column(name = "storage_path", length = 255, nullable = false)
    private String storagePath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private KycStatus status;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}
