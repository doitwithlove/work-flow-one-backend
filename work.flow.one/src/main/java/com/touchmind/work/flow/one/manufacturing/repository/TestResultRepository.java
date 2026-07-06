package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.TestResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TestResultRepository extends ReactiveMongoRepository<TestResult, String> {

    Flux<TestResult> findByPartIdOrderByTestedAtDesc(String partId);

    Flux<TestResult> findByMachineIdOrderByTestedAtDesc(String machineId);

    Mono<TestResult> findFirstByPartIdOrderByTestedAtDesc(String partId);
}
