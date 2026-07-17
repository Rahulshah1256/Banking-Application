package com.jantabank.security;

import com.jantabank.entity.Role;
import com.jantabank.entity.User;
import com.jantabank.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private String jwtExpirationDate;

    private final UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //Generate JWT Token from an authenticated principal
    public String generateToken(Authentication authentication) {
        return generateAccessToken(authentication.getName());
    }

    //Generate JWT access token for a given username
    public String generateAccessToken(String username) {
        long jwtExpirationDateLong = Long.parseLong(jwtExpirationDate);
        Date issueDate = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(issueDate.getTime() + jwtExpirationDateLong);

        Optional<User> optionalUser = userRepository.findByUsernameOrEmail(username, username);
        if (optionalUser.isPresent()) {
            User loggedInUser = optionalUser.get();
            Optional<Role> optionalRole = loggedInUser.getRoles().stream().findFirst();

            if (optionalRole.isPresent()) {
                Role userrole = optionalRole.get();

                Claims claims = Jwts.claims().setSubject(username);
                claims.put("role", userrole.getName());
                claims.put("email", loggedInUser.getEmail());
                claims.put("name", loggedInUser.getName());
                claims.put("id", loggedInUser.getId());

                return Jwts.builder()
                        .setClaims(claims)
                        .setId(UUID.randomUUID().toString())
                        .setIssuedAt(issueDate)
                        .setExpiration(expiryDate)
                        .signWith(key())
                        .compact();
            }
        }
        return "";
    }

    private Key key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    //Get Username From JWT Token
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    //Get the unique token id (jti) from a JWT
    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    //Get the expiry instant of a JWT as LocalDateTime
    public LocalDateTime getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(expiration.getTime()), ZoneId.systemDefault());
    }

    //Configured access-token lifetime in seconds
    public long getAccessExpirationSeconds() {
        return Long.parseLong(jwtExpirationDate) / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(600000)
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //Validate JWT Token
    public boolean validateToken(String token) {
        Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(600000)
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token);
        return true;
    }

}

