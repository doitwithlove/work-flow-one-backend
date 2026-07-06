package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.CreateOperatorRequest;
import com.touchmind.work.flow.one.manufacturing.dto.OperatorResponse;
import com.touchmind.work.flow.one.manufacturing.dto.UpdateOperatorRequest;
import com.touchmind.work.flow.one.manufacturing.service.OperatorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/operators")
public class OperatorController {

    private final OperatorService operatorService;

    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @PostMapping
    public Mono<OperatorResponse> create(@Valid @RequestBody CreateOperatorRequest request) {
        return operatorService.create(request);
    }

    @GetMapping
    public Flux<OperatorResponse> list() {
        return operatorService.list();
    }

    @GetMapping("/{id}")
    public Mono<OperatorResponse> get(@PathVariable String id) {
        return operatorService.get(id);
    }

    @PutMapping("/{id}")
    public Mono<OperatorResponse> update(@PathVariable String id, @RequestBody UpdateOperatorRequest request) {
        return operatorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return operatorService.delete(id);
    }
}
