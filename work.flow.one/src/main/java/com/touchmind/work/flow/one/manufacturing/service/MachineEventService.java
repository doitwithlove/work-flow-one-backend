package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.manufacturing.domain.MachineEvent;
import com.touchmind.work.flow.one.manufacturing.dto.MachineEventRequest;
import com.touchmind.work.flow.one.manufacturing.dto.MachineEventResponse;
import com.touchmind.work.flow.one.manufacturing.enums.MachineEventType;
import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineEventRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
public class MachineEventService {

    private final MachineEventRepository machineEventRepository;
    private final MachineService machineService;
    private final PartService partService;
    private final OperatorSessionService operatorSessionService;

    public MachineEventService(
            MachineEventRepository machineEventRepository,
            MachineService machineService,
            PartService partService,
            OperatorSessionService operatorSessionService) {
        this.machineEventRepository = machineEventRepository;
        this.machineService = machineService;
        this.partService = partService;
        this.operatorSessionService = operatorSessionService;
    }

    public Mono<MachineEventResponse> ingest(MachineEventRequest request) {
        String machineId = request.machineId().trim();
        return operatorSessionService.findActiveSessionByMachine(machineId)
                .flatMap(session -> saveEvent(request, machineId, session.id(), session.operatorId(), request.status().trim()))
                .switchIfEmpty(saveEvent(request, machineId, null, null, "UNASSIGNED"));
    }

    public Flux<MachineEventResponse> list() {
        return machineEventRepository.findAll()
                .sort((left, right) -> right.receivedAt().compareTo(left.receivedAt()))
                .map(this::toResponse);
    }

    public Flux<MachineEventResponse> listByPart(String partId) {
        return machineEventRepository.findByPartIdOrderByReceivedAtDesc(partId).map(this::toResponse);
    }

    public Mono<MachineEventResponse> latestByPart(String partId) {
        return machineEventRepository.findFirstByPartIdOrderByReceivedAtDesc(partId).map(this::toResponse);
    }

    private Mono<MachineEventResponse> saveEvent(
            MachineEventRequest request,
            String machineId,
            String operatorSessionId,
            String operatorId,
            String status) {
        MachineEvent event = new MachineEvent(
                null,
                machineId,
                request.partId().trim(),
                operatorId,
                operatorSessionId,
                request.eventType(),
                status,
                request.payload() == null ? Map.of() : request.payload(),
                Instant.now());

        return machineEventRepository.save(event)
                .flatMap(saved -> applyWorkflow(saved).thenReturn(saved))
                .map(this::toResponse);
    }

    private Mono<Void> applyWorkflow(MachineEvent event) {
        Mono<Void> machineUpdate = machineService.findByMachineCode(event.machineId())
                .flatMap(machine -> machineService.markStatus(machine.machineCode(), resolveMachineStatus(event)))
                .then();

        Mono<Void> partUpdate;
        if (event.eventType() == MachineEventType.PROCESS_STARTED) {
            partUpdate = partService.markInProcess(event.partId(), event.machineId()).then();
        } else if (event.eventType() == MachineEventType.PROCESS_COMPLETED) {
            partUpdate = partService.markTestPending(event.partId(), event.machineId()).then();
        } else if (event.eventType() == MachineEventType.ERROR) {
            partUpdate = partService.markFailure(event.partId(), event.machineId(), PartStatus.FAILED).then();
        } else {
            partUpdate = partService.saveMachineContext(event.partId(), event.machineId(), PartStatus.IN_PROCESS, TestStatus.PENDING).then();
        }

        return machineUpdate.then(partUpdate);
    }

    private MachineStatus resolveMachineStatus(MachineEvent event) {
        if (event.eventType() == MachineEventType.ERROR || "ERROR".equalsIgnoreCase(event.status())) {
            return MachineStatus.ERROR;
        }
        if (event.eventType() == MachineEventType.PROCESS_COMPLETED || "COMPLETED".equalsIgnoreCase(event.status())) {
            return MachineStatus.IDLE;
        }
        if (event.eventType() == MachineEventType.PROCESS_STARTED || "RUNNING".equalsIgnoreCase(event.status()) || "IN_PROCESS".equalsIgnoreCase(event.status())) {
            return MachineStatus.RUNNING;
        }
        return MachineStatus.IDLE;
    }

    private MachineEventResponse toResponse(MachineEvent event) {
        return new MachineEventResponse(
                event.id(),
                event.machineId(),
                event.partId(),
                event.operatorId(),
                event.operatorSessionId(),
                event.eventType(),
                event.status(),
                event.payload(),
                event.receivedAt());
    }
}
