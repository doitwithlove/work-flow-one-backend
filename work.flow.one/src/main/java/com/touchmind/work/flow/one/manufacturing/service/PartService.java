package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.manufacturing.domain.Part;
import com.touchmind.work.flow.one.manufacturing.domain.ProcessStep;
import com.touchmind.work.flow.one.manufacturing.dto.CreatePartRequest;
import com.touchmind.work.flow.one.manufacturing.dto.PartHistoryResponse;
import com.touchmind.work.flow.one.manufacturing.dto.PartResponse;
import com.touchmind.work.flow.one.manufacturing.dto.UpdatePartStatusRequest;
import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineEventRepository;
import com.touchmind.work.flow.one.manufacturing.repository.PartRepository;
import com.touchmind.work.flow.one.manufacturing.repository.TestResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class PartService {

    private final PartRepository partRepository;
    private final ProcessStepService processStepService;
    private final MachineEventRepository machineEventRepository;
    private final TestResultRepository testResultRepository;

    public PartService(
            PartRepository partRepository,
            ProcessStepService processStepService,
            MachineEventRepository machineEventRepository,
            TestResultRepository testResultRepository) {
        this.partRepository = partRepository;
        this.processStepService = processStepService;
        this.machineEventRepository = machineEventRepository;
        this.testResultRepository = testResultRepository;
    }

    public Mono<PartResponse> create(CreatePartRequest request) {
        Mono<String> currentStepIdMono = request.currentStepId() != null && !request.currentStepId().isBlank()
                ? Mono.just(request.currentStepId().trim())
                : processStepService.firstStep().map(ProcessStep::id);

        return currentStepIdMono.flatMap(stepId -> {
            Instant now = Instant.now();
            Part part = new Part(
                    null,
                    request.partNumber().trim(),
                    request.batchNumber().trim(),
                    stepId,
                    null,
                    PartStatus.CREATED,
                    TestStatus.PENDING,
                    now,
                    now);
            return partRepository.save(part).map(this::toResponse);
        });
    }

    public Flux<PartResponse> list() {
        return partRepository.findAll().sort((left, right) -> right.createdAt().compareTo(left.createdAt())).map(this::toResponse);
    }

    public Mono<PartResponse> get(String id) {
        return partRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Part not found")))
                .map(this::toResponse);
    }

    public Mono<Part> findDomain(String id) {
        return partRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Part not found")));
    }

    public Mono<PartResponse> updateStatus(String id, UpdatePartStatusRequest request) {
        return findDomain(id)
                .flatMap(part -> partRepository.save(new Part(
                        part.id(),
                        part.partNumber(),
                        part.batchNumber(),
                        request.currentStepId() != null ? request.currentStepId() : part.currentStepId(),
                        request.currentMachineId() != null ? request.currentMachineId() : part.currentMachineId(),
                        request.status(),
                        request.testStatus() != null ? request.testStatus() : part.testStatus(),
                        part.createdAt(),
                        Instant.now())))
                .map(this::toResponse);
    }

    public Mono<PartResponse> markInProcess(String partId, String machineId) {
        return findDomain(partId)
                .flatMap(part -> saveLifecycle(part, PartStatus.IN_PROCESS, TestStatus.PENDING, machineId));
    }

    public Mono<PartResponse> markTestPending(String partId, String machineId) {
        return findDomain(partId)
                .flatMap(part -> saveLifecycle(part, PartStatus.TEST_PENDING, TestStatus.PENDING, machineId));
    }

    public Mono<PartResponse> markReadyForNextPhase(String partId, String machineId) {
        return findDomain(partId)
                .flatMap(part -> saveLifecycle(part, PartStatus.READY_FOR_NEXT_PHASE, TestStatus.PASS, machineId));
    }

    public Mono<PartResponse> markFailure(String partId, String machineId, PartStatus status) {
        return findDomain(partId)
                .flatMap(part -> saveLifecycle(part, status, TestStatus.FAIL, machineId));
    }

    public Mono<PartResponse> moveNext(String partId) {
        return findDomain(partId)
                .flatMap(part -> {
                    if (part.status() != PartStatus.READY_FOR_NEXT_PHASE) {
                        return Mono.error(new ApiException(HttpStatus.CONFLICT, "Part is not ready for the next phase"));
                    }

                    Mono<ProcessStep> currentStepMono = part.currentStepId() != null
                            ? processStepService.getById(part.currentStepId())
                            : processStepService.firstStep();

                    return currentStepMono.flatMap(currentStep ->
                            processStepService.resolveNextStep(currentStep)
                                    .flatMap(nextStep -> partRepository.save(new Part(
                                            part.id(),
                                            part.partNumber(),
                                            part.batchNumber(),
                                            nextStep.id(),
                                            null,
                                            PartStatus.WAITING,
                                            TestStatus.PENDING,
                                            part.createdAt(),
                                            Instant.now())))
                                    .switchIfEmpty(partRepository.save(new Part(
                                            part.id(),
                                            part.partNumber(),
                                            part.batchNumber(),
                                            currentStep.nextStepId(),
                                            null,
                                            PartStatus.COMPLETED,
                                            TestStatus.PASS,
                                            part.createdAt(),
                                            Instant.now()))));
                })
                .map(this::toResponse);
    }

    public Mono<PartHistoryResponse> history(String id) {
        Mono<PartResponse> part = get(id);
        return Mono.zip(
                        part,
                        machineEventRepository.findByPartIdOrderByReceivedAtDesc(id).map(event -> new com.touchmind.work.flow.one.manufacturing.dto.MachineEventResponse(
                                event.id(),
                                event.machineId(),
                                event.partId(),
                                event.operatorId(),
                                event.operatorSessionId(),
                                event.eventType(),
                                event.status(),
                                event.payload(),
                                event.receivedAt())).collectList(),
                        testResultRepository.findByPartIdOrderByTestedAtDesc(id).map(result -> new com.touchmind.work.flow.one.manufacturing.dto.TestResultResponse(
                                result.id(),
                                result.partId(),
                                result.machineId(),
                                result.testType(),
                                result.expectedValue(),
                                result.actualValue(),
                                result.toleranceMin(),
                                result.toleranceMax(),
                                result.result(),
                                result.testedAt())).collectList())
                .map(tuple -> new PartHistoryResponse(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    public Mono<Part> saveMachineContext(String partId, String machineId, PartStatus status, TestStatus testStatus) {
        return findDomain(partId)
                .flatMap(part -> partRepository.save(new Part(
                        part.id(),
                        part.partNumber(),
                        part.batchNumber(),
                        part.currentStepId(),
                        machineId,
                        status,
                        testStatus,
                        part.createdAt(),
                        Instant.now())));
    }

    private Mono<PartResponse> saveLifecycle(Part part, PartStatus status, TestStatus testStatus, String machineId) {
        return partRepository.save(new Part(
                part.id(),
                part.partNumber(),
                part.batchNumber(),
                part.currentStepId(),
                machineId != null ? machineId : part.currentMachineId(),
                status,
                testStatus,
                part.createdAt(),
                Instant.now()))
                .map(this::toResponse);
    }

    private PartResponse toResponse(Part part) {
        return new PartResponse(
                part.id(),
                part.partNumber(),
                part.batchNumber(),
                part.currentStepId(),
                part.currentMachineId(),
                part.status(),
                part.testStatus(),
                part.createdAt(),
                part.updatedAt());
    }
}
