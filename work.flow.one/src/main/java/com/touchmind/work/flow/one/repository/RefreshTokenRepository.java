package com.touchmind.work.flow.one.repository;

import com.touchmind.work.flow.one.model.RefreshToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveMongoRepository<RefreshToken, String> {

    Mono<RefreshToken> findByToken(String token);

    Mono<Void> deleteByUsername(String username);
}
