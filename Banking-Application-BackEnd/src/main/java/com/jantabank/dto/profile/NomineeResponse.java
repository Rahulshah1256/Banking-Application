package com.jantabank.dto.profile;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class NomineeResponse {
    private Long id;
    private String name;
    private String relationship;
    private double sharePercentage;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
}
