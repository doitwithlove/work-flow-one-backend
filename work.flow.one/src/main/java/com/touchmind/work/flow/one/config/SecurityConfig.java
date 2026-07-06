package com.touchmind.work.flow.one.config;

import com.touchmind.work.flow.one.security.JwtAuthenticationConverter;
import com.touchmind.work.flow.one.security.JwtProperties;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationConverter converter;
    private final JwtProperties properties;

    public SecurityConfig(JwtAuthenticationConverter converter, JwtProperties properties) {
        this.converter = converter;
        this.properties = properties;
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {

        SecretKey key = Keys.hmacShaKeyFor(
                properties.getSecret()
                        .getBytes(StandardCharsets.UTF_8));

        return NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();

    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                        .pathMatchers("/api/auth/me", "/api/auth/logout").authenticated()
                        .pathMatchers("/api/sample/**").permitAll()
                        .pathMatchers("/api/super-user/**").hasAnyRole("SUPER_USER", "ADMIN")
                        .pathMatchers("/api/dashboard/**").hasAnyRole("SUPER_USER", "ADMIN", "MANAGER", "SUPERVISOR")
                        .pathMatchers("/api/machines/**").hasAnyRole("SUPER_USER", "ADMIN", "MANAGER", "SUPERVISOR", "OPERATOR")
                        .pathMatchers("/api/parts/**").hasAnyRole("SUPER_USER", "ADMIN", "MANAGER", "SUPERVISOR", "OPERATOR", "QUALITY_INSPECTOR")
                        .pathMatchers("/api/operators/**").hasAnyRole("SUPER_USER", "ADMIN", "MANAGER", "SUPERVISOR")
                        .pathMatchers("/api/operator-sessions/**").hasAnyRole("SUPER_USER", "ADMIN", "MANAGER", "SUPERVISOR", "OPERATOR")
                        .pathMatchers("/api/productivity/**").hasAnyRole("SUPER_USER", "ADMIN", "MANAGER", "SUPERVISOR")
                        .pathMatchers("/api/machine-events/**").hasAnyRole("SUPER_USER", "ADMIN", "SUPERVISOR", "OPERATOR")
                        .pathMatchers("/api/shifts/**", "/api/process-steps/**", "/api/test-results/**", "/api/users/me", "/api/users/me/**").authenticated()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/api/admin/**").hasAnyRole("SUPER_USER", "ADMIN")
                        .pathMatchers("/api/users/**").authenticated()
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter)))
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174",
                "http://localhost:4173",
                "http://127.0.0.1:4173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {
            exchange.getResponse().setRawStatusCode(401);
            return exchange.getResponse().setComplete();
        };
    }

    @Bean
    ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> {
            exchange.getResponse().setRawStatusCode(403);
            return exchange.getResponse().setComplete();
        };
    }
}
