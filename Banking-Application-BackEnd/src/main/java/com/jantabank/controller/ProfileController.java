package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.profile.ProfileResponse;
import com.jantabank.dto.profile.StoredFileContent;
import com.jantabank.dto.profile.UpdateProfileRequest;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.service.ProfileService;
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

@CrossOrigin("*")
@RestController
@RequestMapping("api/profile")
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> get(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile(principal.getName()), "Profile retrieved"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> update(@Valid @RequestBody UpdateProfileRequest request,
                                                               Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(request, principal.getName()),
                "Profile updated"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileResponse>> uploadPhoto(@RequestParam("file") MultipartFile file,
                                                                    Principal principal) {
        byte[] bytes = readBytes(file);
        ProfileResponse response = profileService.uploadPhoto(bytes, file.getContentType(),
                file.getOriginalFilename(), principal.getName());
        return ResponseEntity.ok(ApiResponse.success(response, "Profile photo uploaded"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/photo")
    public ResponseEntity<byte[]> getPhoto(Principal principal) {
        StoredFileContent content = profileService.getPhoto(principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.getContentType()))
                .body(content.getContent());
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
