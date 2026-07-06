package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.manufacturing.domain.ProcessStep;
import com.touchmind.work.flow.one.manufacturing.dto.CreateProcessStepRequest;
import com.touchmind.work.flow.one.manufacturing.dto.ProcessStepResponse;
import com.touchmind.work.flow.one.manufacturing.repository.ProcessStepRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProcessStepService {

    private final ProcessStepRepository processStepRepository;

    public ProcessStepService(ProcessStepRepository processStepRepository) {
        this.processStepRepository = processStepRepository;
    }

    public Mono<ProcessStepResponse> create(CreateProcessStepRequest request) {
        ProcessStep step = new ProcessStep(
                null,
                request.stepNumber(),
                request.name().trim(),
                request.machineType().trim(),
                request.requiredTestType().trim(),
                request.nextStepId());
        return processStepRepository.save(step).map(this::toResponse);
    }

    public Flux<ProcessStepResponse> list() {
        return processStepRepository.findAllByOrderByStepNumberAsc().map(this::toResponse);
    }

    public Mono<ProcessStep> getById(String id) {
        return processStepRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Process step not found")));
    }

    public Mono<ProcessStep> firstStep() {
        return processStepRepository.findAllByOrderByStepNumberAsc().next()
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "No process steps configured")));
    }

    public Mono<ProcessStep> resolveNextStep(ProcessStep currentStep) {
        if (currentStep.nextStepId() == null || currentStep.nextStepId().isBlank()) {
            return Mono.empty();
        }
        return getById(currentStep.nextStepId());
    }

    private ProcessStepResponse toResponse(ProcessStep step) {
        return new ProcessStepResponse(
                step.id(),
                step.stepNumber(),
                step.name(),
                step.machineType(),
                step.requiredTestType(),
                step.nextStepId());
    }
}
