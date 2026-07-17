package com.jantabank.dto.profile;

import com.jantabank.domain.enums.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Editable profile attributes. Identity fields (PAN, Aadhaar, username, email)
 * are intentionally not editable here.
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 500, message = "Address is too long")
    private String address;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 100, message = "Occupation is too long")
    private String occupation;

    @PositiveOrZero(message = "Annual income cannot be negative")
    private Double annualIncome;

    @Size(max = 500, message = "Communication address is too long")
    private String communicationAddress;

    @Size(max = 500, message = "Permanent address is too long")
    private String permanentAddress;

    @Size(max = 20, message = "Alternate phone is too long")
    private String alternatePhone;
}
