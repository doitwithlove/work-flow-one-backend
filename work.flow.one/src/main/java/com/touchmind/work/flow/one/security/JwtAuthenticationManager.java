package com.touchmind.work.flow.one.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationManager
        implements ReactiveAuthenticationManager {

    private final JwtService jwtService;

    private final ReactiveUserDetailsServiceImpl users;

    public JwtAuthenticationManager(JwtService jwtService, ReactiveUserDetailsServiceImpl users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication){

        String token=authentication.getCredentials().toString();

        String username=jwtService.extractUsername(token);

        return users.findByUsername(username)
                .filter(user->jwtService.validate(token))
                .map(user->new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                ));
    }

}
