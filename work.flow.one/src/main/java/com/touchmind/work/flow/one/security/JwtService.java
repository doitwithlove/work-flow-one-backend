package com.touchmind.work.flow.one.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal user) {

        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return buildToken(user.getUsername(), roles, properties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(UserPrincipal user) {
        return buildToken(user.getUsername(), List.of(), properties.getRefreshTokenExpiration());
    }

    public Instant accessTokenExpiry() {
        return Instant.now().plusMillis(properties.getAccessTokenExpiration());
    }

    public Instant refreshTokenExpiry() {
        return Instant.now().plusMillis(properties.getRefreshTokenExpiration());
    }

    public long accessTokenExpiresInSeconds() {
        return properties.getAccessTokenExpiration() / 1000;
    }

    private String buildToken(String subject, List<String> roles, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {

        return extractClaims(token).getSubject();

    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {

        return extractClaims(token)
                .get("roles", List.class);

    }

    public boolean validate(String token) {

        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

}
