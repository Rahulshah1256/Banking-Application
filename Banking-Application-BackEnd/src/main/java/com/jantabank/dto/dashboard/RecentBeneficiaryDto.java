package com.jantabank.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentBeneficiaryDto {
    private long id;
    private String name;
    private String maskedAccountNumber;
    private String ifscCode;
    private String status;
}
