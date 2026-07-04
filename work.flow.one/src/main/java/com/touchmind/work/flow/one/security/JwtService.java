package com.touchmind.work.flow.one.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal user) {

        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();

        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {

        Date now = new Date();

        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(user.getUsername())
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