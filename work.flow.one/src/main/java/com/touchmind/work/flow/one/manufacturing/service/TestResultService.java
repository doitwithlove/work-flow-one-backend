package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.manufacturing.domain.TestResult;
import com.touchmind.work.flow.one.manufacturing.dto.CreateTestResultRequest;
import com.touchmind.work.flow.one.manufacturing.dto.TestResultResponse;
import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestResultStatus;
import com.touchmind.work.flow.one.manufacturing.repository.TestResultRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final PartService partService;
    private final ProductivityService productivityService;

    public TestResultService(TestResultRepository testResultRepository, PartService partService, ProductivityService productivityService) {
        this.testResultRepository = testResultRepository;
        this.partService = partService;
        this.productivityService = productivityService;
    }

    public Mono<TestResultResponse> create(CreateTestResultRequest request) {
        TestResult result = new TestResult(
                null,
                request.partId().trim(),
                request.machineId().trim(),
                request.testType().trim(),
                request.expectedValue(),
                request.actualValue(),
                request.toleranceMin(),
                request.toleranceMax(),
                request.result(),
                Instant.now());

        return testResultRepository.save(result)
                .flatMap(saved -> applyBusinessRule(saved).then(productivityService.recordTestResult(saved)).thenReturn(saved))
                .map(this::toResponse);
    }

    public Flux<TestResultResponse> listByPart(String partId) {
        return testResultRepository.findByPartIdOrderByTestedAtDesc(partId).map(this::toResponse);
    }

    private Mono<Void> applyBusinessRule(TestResult result) {
        if (result.result() == TestResultStatus.PASS) {
            return partService.markReadyForNextPhase(result.partId(), result.machineId()).then();
        }

        return partService.markFailure(result.partId(), result.machineId(), determineFailureStatus(result)).then();
    }

    private PartStatus determineFailureStatus(TestResult result) {
        double actual = result.actualValue() == null ? 0.0 : result.actualValue();
        double tolerance = Math.max(Math.abs(result.toleranceMin() == null ? 0.0 : result.toleranceMin()),
                Math.abs(result.toleranceMax() == null ? 0.0 : result.toleranceMax()));
        double expected = result.expectedValue() == null ? 0.0 : result.expectedValue();
        return Math.abs(actual - expected) > tolerance ? PartStatus.FAILED : PartStatus.REWORK_REQUIRED;
    }

    private TestResultResponse toResponse(TestResult result) {
        return new TestResultResponse(
                result.id(),
                result.partId(),
                result.machineId(),
                result.testType(),
                result.expectedValue(),
                result.actualValue(),
                result.toleranceMin(),
                result.toleranceMax(),
                result.result(),
                result.testedAt());
    }
}
