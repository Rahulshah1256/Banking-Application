package com.jantabank.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.jantabank.domain.enums.UserStatus;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private long id;

    private String name;

    private String username;

    private String email;

    private String password;

    private String aadhaarno;

    private String panno;

    private String address;

    private String mobile;

    private UserStatus status;
}
