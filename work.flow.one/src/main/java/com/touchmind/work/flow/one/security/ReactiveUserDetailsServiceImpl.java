package com.touchmind.work.flow.one.security;

import com.touchmind.work.flow.one.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl
        implements ReactiveUserDetailsService {

    private final UserRepository repository;

    @Override
    public Mono<UserPrincipal> findByUsername(String username) {

        return repository.findByUsername(username)

                .switchIfEmpty(
                        Mono.error(
                                new UsernameNotFoundException(username)))

                .map(UserPrincipal::new);

    }

}