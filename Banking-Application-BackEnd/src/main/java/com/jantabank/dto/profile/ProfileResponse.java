package com.jantabank.dto.profile;

import com.jantabank.domain.enums.Gender;
import com.jantabank.domain.enums.KycStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProfileResponse {
    private Long userId;
    private String name;
    private String username;
    private String email;
    private boolean emailVerified;
    private String mobile;
    private String maskedPan;
    private String maskedAadhaar;
    private String address;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String occupation;
    private Double annualIncome;
    private String communicationAddress;
    private String permanentAddress;
    private String alternatePhone;
    private boolean hasProfilePhoto;
    private KycStatus kycStatus;
}
