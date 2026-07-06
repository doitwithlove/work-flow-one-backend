package com.touchmind.work.flow.one.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

@Component
public class JwtAuthenticationConverter
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        List<String> roles = Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                .orElse(Collections.emptyList());

        List<GrantedAuthority> authorities = roles.stream()
                .map(JwtAuthenticationConverter::normalizeAuthority)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        return Mono.just(
                new JwtAuthenticationToken(jwt, authorities));

    }

    private static String normalizeAuthority(String role) {
        if (role == null || role.isBlank()) {
            return role;
        }

        String trimmed = role.trim();
        if (trimmed.startsWith("ROLE_")) {
            return trimmed;
        }

        return "ROLE_" + trimmed.toUpperCase(Locale.ROOT);
    }

}
