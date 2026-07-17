package com.jantabank.service;
import com.jantabank.dto.JwtAuthResponse;
import com.jantabank.dto.LoginDto;
import com.jantabank.dto.RefreshTokenRequest;
import com.jantabank.dto.RegisterDto;
import com.jantabank.web.ClientMetadata;

public interface AuthService {
    String register(RegisterDto registerDto);

    JwtAuthResponse login(LoginDto loginDto, ClientMetadata metadata);

    JwtAuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String accessToken, RefreshTokenRequest request);
}
