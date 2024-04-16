package com.jantabank.utils;

import com.jantabank.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    public UserDto extractClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(600000)
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        UserDto userDto = new UserDto();
        userDto.setEmail((String) claims.get("email"));
        userDto.setName((String) claims.get("name"));
        userDto.setId((int) claims.get("id"));
        return userDto;
    }
}
