package com.jantabank.dto.profile;

import com.jantabank.domain.enums.KycStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class KycStatusResponse {
    private KycStatus overallStatus;
    private long totalDocuments;
    private long verifiedDocuments;
    private long pendingDocuments;
    private long rejectedDocuments;
    private List<KycDocumentResponse> documents;
}
