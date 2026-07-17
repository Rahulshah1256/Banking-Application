package com.jantabank.impl;
import com.jantabank.dto.JwtAuthResponse;
import com.jantabank.dto.LoginDto;
import com.jantabank.dto.RefreshTokenRequest;
import com.jantabank.dto.RegisterDto;
import com.jantabank.entity.RefreshToken;
import com.jantabank.entity.Role;
import com.jantabank.entity.User;
import com.jantabank.domain.enums.UserStatus;
import com.jantabank.exception.TodoAPIException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jantabank.repository.RoleRepository;
import com.jantabank.repository.UserRepository;
import com.jantabank.security.JwtTokenProvider;
import com.jantabank.service.AuthService;
import com.jantabank.service.DeviceService;
import com.jantabank.service.EmailVerificationService;
import com.jantabank.service.LoginAttemptService;
import com.jantabank.service.LoginHistoryService;
import com.jantabank.service.RefreshTokenService;
import com.jantabank.service.TokenBlacklistService;
import com.jantabank.web.ClientMetadata;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private RefreshTokenService refreshTokenService;
    private TokenBlacklistService tokenBlacklistService;
    private EmailVerificationService emailVerificationService;
    private LoginAttemptService loginAttemptService;
    private LoginHistoryService loginHistoryService;
    private DeviceService deviceService;
    @Override
    public String register(RegisterDto registerDto) {

        log.info("Registration requested for username='{}'", registerDto.getUsername());
        //check user exists
        if(userRepository.existsByUsername(registerDto.getUsername())) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,"Username already exists!");
        }
        //check email exists
        if(userRepository.existsByEmail(registerDto.getEmail())) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST,"Email already exists!");
        }
        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setAadhaarNo(registerDto.getAadhaarno());
        user.setPanNo(registerDto.getPanno());
        user.setPanNo(registerDto.getPanno());
        user.setAddress(registerDto.getAddress());
        user.setMobile(registerDto.getMobile());
        user.setStatus(UserStatus.REQUESTED);

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER");
        roles.add(userRole);

        user.setRoles(roles);

        userRepository.save(user);

        emailVerificationService.sendVerification(user);

        log.info("User '{}' registered successfully with status REQUESTED", registerDto.getUsername());
        return "User registered successfully";
    }

    @Override
    public JwtAuthResponse login(LoginDto loginDto, ClientMetadata metadata) {
        String identifier = loginDto.getUsernameoremail();
        log.info("Login attempt for '{}'", identifier);

        User user = userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);

        if (user != null && user.isLocked()) {
            loginHistoryService.record(user, identifier, false, "ACCOUNT_LOCKED", metadata);
            log.warn("Login blocked for locked account '{}'", identifier);
            throw new TodoAPIException(HttpStatus.LOCKED, loginAttemptService.lockMessage(user));
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    identifier, loginDto.getPassword()
            ));
        } catch (AuthenticationException ex) {
            if (user != null) {
                loginAttemptService.recordFailure(user.getId());
            }
            loginHistoryService.record(user, identifier, false, "BAD_CREDENTIALS", metadata);
            log.warn("Login failed for '{}': {}", identifier, ex.getMessage());
            throw ex;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User authenticatedUser = loadUser(authentication.getName());
        loginAttemptService.recordSuccess(authenticatedUser);

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(authenticatedUser);

        loginHistoryService.record(authenticatedUser, identifier, true, null, metadata);
        deviceService.registerOrUpdate(authenticatedUser, metadata);

        log.info("Login successful for '{}'", identifier);
        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationSeconds())
                .build();
    }

    @Override
    public JwtAuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken current = refreshTokenService.verify(request.getRefreshToken());
        User user = current.getUser();
        String newRefreshToken = refreshTokenService.rotate(request.getRefreshToken());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());

        log.info("Access token refreshed for '{}'", user.getUsername());
        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationSeconds())
                .build();
    }

    @Override
    public void logout(String accessToken, RefreshTokenRequest request) {
        if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
            tokenBlacklistService.blacklist(jwtTokenProvider.getJti(accessToken),
                    jwtTokenProvider.getExpiration(accessToken));
        }
        if (request != null) {
            refreshTokenService.revoke(request.getRefreshToken());
        }
        SecurityContextHolder.clearContext();
        log.info("Logout completed");
    }

    private User loadUser(String username) {
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new TodoAPIException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}

