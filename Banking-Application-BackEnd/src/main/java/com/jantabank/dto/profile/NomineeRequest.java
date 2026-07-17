package com.jantabank.dto.profile;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NomineeRequest {

    @NotBlank(message = "Nominee name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Relationship is required")
    @Size(max = 50)
    private String relationship;

    @Positive(message = "Share percentage must be greater than zero")
    @DecimalMax(value = "100.0", message = "Share percentage cannot exceed 100")
    private double sharePercentage;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String address;
}
