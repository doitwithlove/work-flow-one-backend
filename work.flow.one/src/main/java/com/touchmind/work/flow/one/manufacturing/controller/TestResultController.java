package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.CreateTestResultRequest;
import com.touchmind.work.flow.one.manufacturing.dto.TestResultResponse;
import com.touchmind.work.flow.one.manufacturing.service.TestResultService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/test-results")
public class TestResultController {

    private final TestResultService testResultService;

    public TestResultController(TestResultService testResultService) {
        this.testResultService = testResultService;
    }

    @PostMapping
    public Mono<TestResultResponse> create(@Valid @RequestBody CreateTestResultRequest request) {
        return testResultService.create(request);
    }

    @GetMapping("/part/{partId}")
    public Flux<TestResultResponse> listByPart(@PathVariable String partId) {
        return testResultService.listByPart(partId);
    }
}
