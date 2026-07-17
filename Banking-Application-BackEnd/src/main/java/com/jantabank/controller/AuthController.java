package com.jantabank.controller;

import com.jantabank.common.ApiResponse;
import com.jantabank.dto.ChangePasswordRequest;
import com.jantabank.dto.ForgotPasswordRequest;
import com.jantabank.dto.JwtAuthResponse;
import com.jantabank.dto.LoginDto;
import com.jantabank.dto.LoginHistoryDto;
import com.jantabank.dto.OtpChallengeResponse;
import com.jantabank.dto.OtpRequestRequest;
import com.jantabank.dto.OtpVerifyRequest;
import com.jantabank.dto.RefreshTokenRequest;
import com.jantabank.dto.RegisterDto;
import com.jantabank.dto.ResendVerificationRequest;
import com.jantabank.dto.ResetPasswordRequest;
import com.jantabank.dto.UserDeviceDto;
import com.jantabank.dto.VerifyEmailRequest;
import com.jantabank.web.ClientMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.jantabank.service.AuthService;
import com.jantabank.service.DeviceService;
import com.jantabank.service.EmailVerificationService;
import com.jantabank.service.LoginHistoryService;
import com.jantabank.service.OtpService;
import com.jantabank.service.PasswordResetService;
import com.jantabank.repository.UserRepository;
import com.jantabank.entity.User;
import com.jantabank.exception.ResourceNotFoundException;

import java.security.Principal;
import java.util.List;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private AuthService authService;
    private PasswordResetService passwordResetService;
    private EmailVerificationService emailVerificationService;
    private OtpService otpService;
    private LoginHistoryService loginHistoryService;
    private DeviceService deviceService;
    private UserRepository userRepository;


    //build register REST API
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto){
        String response =authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //build login REST API
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginDto loginDto,
                                                 HttpServletRequest httpRequest){
        JwtAuthResponse authResponse = authService.login(loginDto, ClientMetadata.from(httpRequest));
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    //build refresh-token REST API
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request){
        JwtAuthResponse authResponse = authService.refreshToken(request);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    //build logout REST API
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpServletRequest httpRequest,
                                                      @RequestBody(required = false) RefreshTokenRequest request){
        authService.logout(resolveAccessToken(httpRequest), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    //build change-password REST API (authenticated)
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(Principal principal,
                                                             @Valid @RequestBody ChangePasswordRequest request){
        passwordResetService.changePassword(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully. Please log in again."));
    }

    //build forgot-password REST API
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null,
                "If an account exists for that email, a password reset link has been sent."));
    }

    //build reset-password REST API
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully. Please log in again."));
    }

    //build verify-email REST API
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request){
        emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully."));
    }

    //build resend-verification REST API
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Object>> resendVerification(@Valid @RequestBody ResendVerificationRequest request){
        emailVerificationService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.success(null,
                "If an unverified account exists for that email, a verification link has been sent."));
    }

    //build OTP-login challenge REST API (step 1)
    @PostMapping("/otp/request")
    public ResponseEntity<OtpChallengeResponse> requestOtp(@Valid @RequestBody OtpRequestRequest request,
                                                           HttpServletRequest httpRequest){
        return ResponseEntity.ok(otpService.requestOtp(request, ClientMetadata.from(httpRequest)));
    }

    //build OTP-login verify REST API (step 2)
    @PostMapping("/otp/verify")
    public ResponseEntity<JwtAuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request,
                                                     HttpServletRequest httpRequest){
        return ResponseEntity.ok(otpService.verifyOtp(request, ClientMetadata.from(httpRequest)));
    }

    //build login-history REST API (authenticated)
    @GetMapping("/login-history")
    public ResponseEntity<ApiResponse<Page<LoginHistoryDto>>> loginHistory(Principal principal,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size){
        long userId = currentUserId(principal);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<LoginHistoryDto> history = loginHistoryService.getHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history, "Login history retrieved"));
    }

    //build last-login REST API (authenticated)
    @GetMapping("/last-login")
    public ResponseEntity<ApiResponse<LoginHistoryDto>> lastLogin(Principal principal){
        long userId = currentUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(
                loginHistoryService.getLastSuccessfulLogin(userId), "Last login retrieved"));
    }

    //build device-list REST API (authenticated)
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<UserDeviceDto>>> devices(Principal principal){
        long userId = currentUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(deviceService.listDevices(userId), "Devices retrieved"));
    }

    //build device-revoke REST API (authenticated)
    @DeleteMapping("/devices/{id}")
    public ResponseEntity<ApiResponse<Object>> revokeDevice(Principal principal, @PathVariable long id){
        long userId = currentUserId(principal);
        deviceService.revokeDevice(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Device removed successfully"));
    }

    private long currentUserId(Principal principal) {
        User user = userRepository.findByUsernameOrEmail(principal.getName(), principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

}
