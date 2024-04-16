package com.jantabank.controller;

import com.jantabank.dto.JwtAuthResponse;
import com.jantabank.dto.LoginDto;
import com.jantabank.dto.RegisterDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jantabank.service.AuthService;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private AuthService authService;


    //build register REST API
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto){
        String response =authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //build login REST API
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto){
        String token =authService.login(loginDto);
        JwtAuthResponse authResponse = new JwtAuthResponse();
        authResponse.setAccessToken(token);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

}
