package com.jantabank.impl;

import com.jantabank.dto.JwtAuthResponse;
import com.jantabank.dto.OtpChallengeResponse;
import com.jantabank.dto.OtpRequestRequest;
import com.jantabank.dto.OtpVerifyRequest;
import com.jantabank.entity.LoginOtp;
import com.jantabank.entity.User;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.repository.LoginOtpRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.security.JwtTokenProvider;
import com.jantabank.security.TokenHashUtil;
import com.jantabank.service.EmailService;
import com.jantabank.service.LoginAttemptService;
import com.jantabank.service.LoginHistoryService;
import com.jantabank.service.DeviceService;
import com.jantabank.service.OtpService;
import com.jantabank.service.RefreshTokenService;
import com.jantabank.web.ClientMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LoginOtpRepository loginOtpRepository;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final LoginHistoryService loginHistoryService;
    private final DeviceService deviceService;
    private final long otpExpirationMillis;
    private final int otpLength;
    private final int otpMaxAttempts;

    public OtpServiceImpl(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          LoginOtpRepository loginOtpRepository,
                          EmailService emailService,
                          JwtTokenProvider jwtTokenProvider,
                          RefreshTokenService refreshTokenService,
                          LoginAttemptService loginAttemptService,
                          LoginHistoryService loginHistoryService,
                          DeviceService deviceService,
                          @Value("${app.otp-expiration-milliseconds:300000}") long otpExpirationMillis,
                          @Value("${app.otp-length:6}") int otpLength,
                          @Value("${app.otp-max-attempts:5}") int otpMaxAttempts) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.loginOtpRepository = loginOtpRepository;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.loginHistoryService = loginHistoryService;
        this.deviceService = deviceService;
        this.otpExpirationMillis = otpExpirationMillis;
        this.otpLength = otpLength;
        this.otpMaxAttempts = otpMaxAttempts;
    }

    @Override
    @Transactional
    public OtpChallengeResponse requestOtp(OtpRequestRequest request, ClientMetadata metadata) {
        String identifier = request.getUsernameoremail();
        User user = userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);

        if (user != null && user.isLocked()) {
            loginHistoryService.record(user, identifier, false, "ACCOUNT_LOCKED", metadata);
            throw new TodoAPIException(HttpStatus.LOCKED, loginAttemptService.lockMessage(user));
        }

        // Validate credentials first; throws AuthenticationException (-> 401) if invalid.
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    identifier, request.getPassword()));
        } catch (AuthenticationException ex) {
            if (user != null) {
                loginAttemptService.recordFailure(user.getId());
            }
            loginHistoryService.record(user, identifier, false, "BAD_CREDENTIALS", metadata);
            throw ex;
        }

        if (user == null) {
            user = loadUser(identifier);
        }
        loginAttemptService.recordSuccess(user);
        loginOtpRepository.consumeAllForUser(user.getId());

        String rawOtp = TokenHashUtil.generateNumericOtp(otpLength);
        LoginOtp otp = new LoginOtp();
        otp.setOtpHash(TokenHashUtil.sha256Hex(rawOtp));
        otp.setUser(user);
        otp.setExpiryDate(LocalDateTime.now().plusNanos(otpExpirationMillis * 1_000_000));
        otp.setConsumed(false);
        otp.setAttempts(0);
        loginOtpRepository.save(otp);

        emailService.sendLoginOtp(user.getEmail(), user.getName(), rawOtp);
        log.info("Login OTP issued for userId={}", user.getId());

        return OtpChallengeResponse.builder()
                .message("An OTP has been sent to your registered email.")
                .expiresInSeconds(otpExpirationMillis / 1000)
                .build();
    }

    @Override
    @Transactional(noRollbackFor = TodoAPIException.class)
    public JwtAuthResponse verifyOtp(OtpVerifyRequest request, ClientMetadata metadata) {
        User user = loadUser(request.getUsernameoremail());

        LoginOtp otp = loginOtpRepository.findFirstByUserIdAndConsumedFalseOrderByIdDesc(user.getId())
                .orElseThrow(() -> new TodoAPIException(HttpStatus.UNAUTHORIZED, "No active OTP. Please request a new one."));

        if (otp.isExpired()) {
            otp.setConsumed(true);
            loginOtpRepository.save(otp);
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "OTP has expired. Please request a new one.");
        }
        if (otp.getAttempts() >= otpMaxAttempts) {
            otp.setConsumed(true);
            loginOtpRepository.save(otp);
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (!otp.getOtpHash().equals(TokenHashUtil.sha256Hex(request.getOtp()))) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= otpMaxAttempts) {
                otp.setConsumed(true);
                loginOtpRepository.save(otp);
                throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "Maximum verification attempts exceeded. Please request a new OTP.");
            }
            loginOtpRepository.save(otp);
            throw new TodoAPIException(HttpStatus.UNAUTHORIZED, "Invalid OTP.");
        }

        otp.setConsumed(true);
        loginOtpRepository.save(otp);

        loginAttemptService.recordSuccess(user);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        loginHistoryService.record(user, request.getUsernameoremail(), true, null, metadata);
        deviceService.registerOrUpdate(user, metadata);
        log.info("OTP login successful for userId={}", user.getId());

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationSeconds())
                .build();
    }

    private User loadUser(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new TodoAPIException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }
}
