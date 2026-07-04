package com.touchmind.work.flow.one.security;

import com.touchmind.work.flow.one.repository.UserRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl
        implements ReactiveUserDetailsService {

    private final UserRepository repository;

    public ReactiveUserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        return repository.findByUsername(username)

                .switchIfEmpty(
                        Mono.error(
                                new UsernameNotFoundException(username)))

                .map(UserPrincipal::new);

    }

}
