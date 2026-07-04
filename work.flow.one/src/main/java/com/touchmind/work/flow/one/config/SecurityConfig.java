package com.touchmind.work.flow.one.config;

import com.touchmind.work.flow.one.security.JwtAuthenticationConverter;
import com.touchmind.work.flow.one.security.JwtProperties;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationConverter converter;
    private final JwtProperties properties;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {

        SecretKey key = Keys.hmacShaKeyFor(
                properties.getSecret()
                        .getBytes(StandardCharsets.UTF_8));

        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .build();

    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http) {

        return http

                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        .pathMatchers("/api/auth/**")
                        .permitAll()

                        .pathMatchers("/swagger-ui/**")
                        .permitAll()

                        .pathMatchers("/v3/api-docs/**")
                        .permitAll()

                        .pathMatchers("/actuator/**")
                        .permitAll()

                        .pathMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .pathMatchers("/user/**")
                        .hasAnyRole("USER", "ADMIN")

                        .anyExchange()
                        .authenticated())

                .oauth2ResourceServer(oauth -> oauth

                        .jwt(jwt -> jwt

                                .jwtAuthenticationConverter(converter)))

                .build();

    }

}