package com.touchmind.work.flow.one.repository;

import com.touchmind.work.flow.one.model.Role;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveMongoRepository<Role, String> {

    Mono<Role> findByName(String name);
}
