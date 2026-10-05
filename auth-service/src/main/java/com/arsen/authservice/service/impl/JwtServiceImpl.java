package com.arsen.authservice.service.impl;

import com.arsen.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String JWT_SECRET_KEY;
    @Value("${application.security.jwt.expiration}")
    private Long JWT_EXPIRATION;

    private static final String ROLES = "roles";
    private static final String USERNAME = "username";
    private static final String IS_ENABLED = "isEnabled";
    private static final String IS_ACCOUNT_NON_LOCKED = "isAccountNonLocked";

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build().parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private String createToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(claims)
                .claim(ROLES, userDetails.getAuthorities())
                .claim(USERNAME, userDetails.getUsername())
                .claim(IS_ENABLED, userDetails.isEnabled())
                .claim(IS_ACCOUNT_NON_LOCKED, userDetails.isAccountNonLocked())
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(JWT_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public long getExpirationMillis(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        long diff = expiration.getTime() - System.currentTimeMillis();
        return Math.max(diff, -1);
    }
}
