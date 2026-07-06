package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.manufacturing.domain.Operator;
import com.touchmind.work.flow.one.manufacturing.dto.CreateOperatorRequest;
import com.touchmind.work.flow.one.manufacturing.dto.OperatorResponse;
import com.touchmind.work.flow.one.manufacturing.dto.UpdateOperatorRequest;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class OperatorService {

    private final OperatorRepository operatorRepository;

    public OperatorService(OperatorRepository operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    public Mono<OperatorResponse> create(CreateOperatorRequest request) {
        Instant now = Instant.now();
        Operator operator = new Operator(
                null,
                request.employeeCode().trim(),
                request.firstName().trim(),
                request.lastName().trim(),
                request.role().trim(),
                request.skillLevel().trim(),
                request.active(),
                now,
                now);
        return operatorRepository.save(operator).map(this::toResponse);
    }

    public Flux<OperatorResponse> list() {
        return operatorRepository.findAll().map(this::toResponse);
    }

    public Mono<OperatorResponse> get(String id) {
        return findDomain(id).map(this::toResponse);
    }

    public Mono<Operator> findDomain(String id) {
        return operatorRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Operator not found")));
    }

    public Mono<OperatorResponse> update(String id, UpdateOperatorRequest request) {
        return findDomain(id)
                .flatMap(operator -> operatorRepository.save(new Operator(
                        operator.id(),
                        operator.employeeCode(),
                        request.firstName() != null ? request.firstName().trim() : operator.firstName(),
                        request.lastName() != null ? request.lastName().trim() : operator.lastName(),
                        request.role() != null ? request.role().trim() : operator.role(),
                        request.skillLevel() != null ? request.skillLevel().trim() : operator.skillLevel(),
                        request.active() != null ? request.active() : operator.active(),
                        operator.createdAt(),
                        Instant.now())))
                .map(this::toResponse);
    }

    public Mono<Void> delete(String id) {
        return operatorRepository.deleteById(id);
    }

    private OperatorResponse toResponse(Operator operator) {
        return new OperatorResponse(
                operator.id(),
                operator.employeeCode(),
                operator.firstName(),
                operator.lastName(),
                operator.role(),
                operator.skillLevel(),
                operator.active(),
                operator.createdAt(),
                operator.updatedAt());
    }
}
