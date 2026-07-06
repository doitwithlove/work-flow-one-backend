package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.Shift;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ShiftRepository extends ReactiveMongoRepository<Shift, String> {

    Mono<Shift> findByName(String name);
}
