package com.jantabank.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Raw binary content for a stored file (profile photo / KYC document download).
 */
@Getter
@AllArgsConstructor
public class StoredFileContent {
    private byte[] content;
    private String contentType;
}
