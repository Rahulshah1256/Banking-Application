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
public class AdminUserSummary {
    private Long id;
    private String name;
    private String username;
    private String maskedEmail;
    private String maskedMobile;
    private UserStatus status;
    private boolean emailVerified;
    private List<String> roles;
    private long accountsCount;
}
