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
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private String jwtExpirationDate;

    private UserRepository userRepository;

    public JwtTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //Generate JWT Token
    public String generateToken(Authentication authentication  )
    {
        String username = authentication.getName();

        Date currentDate= new Date();
        long jwtExpirationDateLong = Long.parseLong(jwtExpirationDate);
        long currentTimeMillis = System.currentTimeMillis();

        Date issueDate = new Date(currentTimeMillis);

        Date expiryDate = new Date(issueDate.getTime() + jwtExpirationDateLong);


        Optional<User> optionalUser = userRepository.findByUsernameOrEmail(username,username);
        if (optionalUser.isPresent())
        {
            User loggedInUser = optionalUser.get();
            Optional<Role> optionalRole = loggedInUser.getRoles().stream().findFirst();

            if(optionalRole.isPresent())
            {
                Role userrole = optionalRole.get();

                Claims claims = Jwts.claims().setSubject(username);
                claims.put("role", userrole.getName());
                claims.put("email", loggedInUser.getEmail());
                claims.put("name", loggedInUser.getName());
                claims.put("id", loggedInUser.getId());

                String token =   Jwts.builder()
                        .setSubject(username)
                        .setClaims(claims)
                        .setIssuedAt(issueDate)
                        .setExpiration(expiryDate)
                        .signWith(key())
                        .compact();

                return token;

            }
        }
        return "";
    }

    private Key key ()
    {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    //Get Username From JWt Token
    public String getUsername(String token)
    {
        Claims claims =  Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(600000)
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String username = claims.getSubject();
        return username;
    }

    //Validate JWT Token

    public boolean validateToken(String token)
    {
        Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(600000)
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token);
        return true;
    }

}

