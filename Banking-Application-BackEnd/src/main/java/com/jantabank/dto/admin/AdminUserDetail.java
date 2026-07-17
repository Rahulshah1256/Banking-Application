package com.jantabank.dto.admin;

import com.jantabank.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetail {
    private Long id;
    private String name;
    private String username;
    private String maskedEmail;
    private String maskedMobile;
    private String maskedPan;
    private String maskedAadhaar;
    private String address;
    private UserStatus status;
    private boolean emailVerified;
    private List<String> roles;
    private List<AdminAccountView> accounts;
    private double totalBalance;
    private long loansCount;
    private long depositsCount;
    private long cardsCount;
    private long kycDocumentsCount;
    private long supportTicketsCount;
}
