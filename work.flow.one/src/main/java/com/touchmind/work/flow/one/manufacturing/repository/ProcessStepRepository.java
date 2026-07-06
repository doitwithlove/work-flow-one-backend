package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.ProcessStep;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProcessStepRepository extends ReactiveMongoRepository<ProcessStep, String> {

    Flux<ProcessStep> findAllByOrderByStepNumberAsc();

    Mono<ProcessStep> findByStepNumber(Integer stepNumber);
}
