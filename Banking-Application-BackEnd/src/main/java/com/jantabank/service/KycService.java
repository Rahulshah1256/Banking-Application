package com.jantabank.service;

import com.jantabank.domain.enums.KycDocumentType;
import com.jantabank.dto.profile.KycDocumentResponse;
import com.jantabank.dto.profile.KycStatusResponse;
import com.jantabank.dto.profile.StoredFileContent;
import com.jantabank.dto.profile.VerifyKycRequest;

import java.util.List;

public interface KycService {

    KycDocumentResponse upload(KycDocumentType type, String documentNumber, byte[] content,
                               String contentType, String originalFilename, String username);

    List<KycDocumentResponse> listMine(String username);

    KycStatusResponse status(String username);

    StoredFileContent download(Long documentId, String username);

    KycDocumentResponse verify(Long documentId, VerifyKycRequest request);
}
