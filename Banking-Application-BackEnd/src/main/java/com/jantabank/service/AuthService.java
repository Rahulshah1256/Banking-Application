package com.jantabank.service;
import com.jantabank.dto.LoginDto;
import com.jantabank.dto.RegisterDto;

public interface AuthService {
    String register(RegisterDto registerDto);

    String login(LoginDto loginDto);
}
