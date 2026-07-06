package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.Operator;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface OperatorRepository extends ReactiveMongoRepository<Operator, String> {

    Mono<Operator> findByEmployeeCode(String employeeCode);
}
