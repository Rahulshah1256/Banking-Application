package com.jantabank.service;

/**
 * Abstraction over binary file storage for KYC documents and profile photos.
 */
public interface FileStorageService {

    /**
     * Persists the given bytes under a sub-directory and returns the relative
     * storage path (portable, stored in the DB).
     */
    String store(byte[] content, String subDirectory, String originalFilename);

    byte[] load(String storagePath);

    void delete(String storagePath);
}
