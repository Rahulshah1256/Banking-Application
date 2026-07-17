package com.jantabank.service;

import com.jantabank.dto.profile.ProfileResponse;
import com.jantabank.dto.profile.StoredFileContent;
import com.jantabank.dto.profile.UpdateProfileRequest;

public interface ProfileService {

    ProfileResponse getProfile(String username);

    ProfileResponse updateProfile(UpdateProfileRequest request, String username);

    ProfileResponse uploadPhoto(byte[] content, String contentType, String originalFilename, String username);

    StoredFileContent getPhoto(String username);
}
