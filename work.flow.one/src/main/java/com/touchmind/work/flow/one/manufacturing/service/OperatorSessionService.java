package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.manufacturing.domain.Operator;
import com.touchmind.work.flow.one.manufacturing.domain.OperatorMachineSession;
import com.touchmind.work.flow.one.manufacturing.domain.Shift;
import com.touchmind.work.flow.one.manufacturing.dto.EndOperatorSessionRequest;
import com.touchmind.work.flow.one.manufacturing.dto.OperatorMachineSessionResponse;
import com.touchmind.work.flow.one.manufacturing.dto.StartOperatorSessionRequest;
import com.touchmind.work.flow.one.manufacturing.enums.OperatorSessionStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineRepository;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorMachineSessionRepository;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorRepository;
import com.touchmind.work.flow.one.manufacturing.repository.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class OperatorSessionService {

    private final OperatorRepository operatorRepository;
    private final MachineRepository machineRepository;
    private final ShiftRepository shiftRepository;
    private final OperatorMachineSessionRepository sessionRepository;

    public OperatorSessionService(
            OperatorRepository operatorRepository,
            MachineRepository machineRepository,
            ShiftRepository shiftRepository,
            OperatorMachineSessionRepository sessionRepository) {
        this.operatorRepository = operatorRepository;
        this.machineRepository = machineRepository;
        this.shiftRepository = shiftRepository;
        this.sessionRepository = sessionRepository;
    }

    public Mono<OperatorMachineSessionResponse> startSession(StartOperatorSessionRequest request) {
        String operatorId = request.operatorId().trim();
        String machineId = request.machineId().trim();
        String shiftId = request.shiftId().trim();

        return validateOperator(operatorId)
                .then(validateMachine(machineId))
                .then(validateShift(shiftId))
                .then(sessionRepository.findFirstByOperatorIdAndStatus(operatorId, OperatorSessionStatus.ACTIVE)
                        .flatMap(existing -> Mono.<OperatorMachineSessionResponse>error(new ApiException(HttpStatus.CONFLICT, "Operator already has an active session"))))
                .then(sessionRepository.findFirstByMachineIdAndStatus(machineId, OperatorSessionStatus.ACTIVE)
                        .flatMap(existing -> Mono.<OperatorMachineSessionResponse>error(new ApiException(HttpStatus.CONFLICT, "Machine already has an active session"))))
                .then(sessionRepository.save(new OperatorMachineSession(
                        null,
                        operatorId,
                        machineId,
                        shiftId,
                        Instant.now(),
                        null,
                        OperatorSessionStatus.ACTIVE)))
                .flatMap(this::toResponse);
    }

    public Mono<OperatorMachineSessionResponse> endSession(EndOperatorSessionRequest request) {
        return sessionRepository.findById(request.sessionId().trim())
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Operator session not found")))
                .flatMap(session -> sessionRepository.save(new OperatorMachineSession(
                        session.id(),
                        session.operatorId(),
                        session.machineId(),
                        session.shiftId(),
                        session.loginTime(),
                        Instant.now(),
                        OperatorSessionStatus.CLOSED)))
                .flatMap(this::toResponse);
    }

    public Flux<OperatorMachineSessionResponse> activeSessions() {
        return sessionRepository.findByStatusOrderByLoginTimeDesc(OperatorSessionStatus.ACTIVE).flatMap(this::toResponse);
    }

    public Flux<OperatorMachineSessionResponse> sessionsByOperator(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            return Flux.empty();
        }

        return sessionRepository.findByOperatorIdOrderByLoginTimeDesc(operatorId.trim()).flatMap(this::toResponse);
    }

    public Flux<OperatorMachineSessionResponse> sessionsByMachine(String machineId) {
        if (machineId == null || machineId.isBlank()) {
            return Flux.empty();
        }

        return sessionRepository.findByMachineIdOrderByLoginTimeDesc(machineId.trim()).flatMap(this::toResponse);
    }

    public Mono<OperatorMachineSession> findActiveSessionByMachine(String machineId) {
        if (machineId == null || machineId.isBlank()) {
            return Mono.empty();
        }

        return sessionRepository.findFirstByMachineIdAndStatus(machineId.trim(), OperatorSessionStatus.ACTIVE);
    }

    public Mono<OperatorMachineSessionResponse> findActiveSessionResponseByMachine(String machineId) {
        if (machineId == null || machineId.isBlank()) {
            return Mono.empty();
        }

        return findActiveSessionByMachine(machineId).flatMap(this::toResponse);
    }

    private Mono<Void> validateOperator(String operatorId) {
        return operatorRepository.findById(operatorId)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Operator not found")))
                .flatMap(this::ensureOperatorActive);
    }

    private Mono<Void> validateMachine(String machineId) {
        return machineRepository.findByMachineCode(machineId)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Machine not found")))
                .then();
    }

    private Mono<Void> validateShift(String shiftId) {
        return shiftRepository.findById(shiftId)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Shift not found")))
                .flatMap(this::ensureShiftActive);
    }

    private Mono<Void> ensureOperatorActive(Operator operator) {
        if (Boolean.FALSE.equals(operator.active())) {
            return Mono.error(new ApiException(HttpStatus.CONFLICT, "Operator is not active"));
        }
        return Mono.empty();
    }

    private Mono<Void> ensureShiftActive(Shift shift) {
        if (Boolean.FALSE.equals(shift.active())) {
            return Mono.error(new ApiException(HttpStatus.CONFLICT, "Shift is not active"));
        }
        return Mono.empty();
    }

    private Mono<OperatorMachineSessionResponse> toResponse(OperatorMachineSession session) {
        Mono<String> operatorName = session.operatorId() == null
                ? Mono.just("Unassigned")
                : operatorRepository.findById(session.operatorId())
                .map(operator -> operator.firstName() + " " + operator.lastName())
                .defaultIfEmpty("Unassigned");

        Mono<String> employeeCode = session.operatorId() == null
                ? Mono.just("UNASSIGNED")
                : operatorRepository.findById(session.operatorId())
                .map(Operator::employeeCode)
                .defaultIfEmpty("UNASSIGNED");

        Mono<String> machineName = session.machineId() == null
                ? Mono.just("Unknown")
                : machineRepository.findByMachineCode(session.machineId())
                .map(machine -> machine.name())
                .defaultIfEmpty(session.machineId());

        Mono<String> shiftName = session.shiftId() == null
                ? Mono.just("Unknown")
                : shiftRepository.findById(session.shiftId())
                .map(Shift::name)
                .defaultIfEmpty(session.shiftId());

        return Mono.zip(operatorName, employeeCode, machineName, shiftName)
                .map(tuple -> new OperatorMachineSessionResponse(
                        session.id(),
                        session.operatorId(),
                        tuple.getT1(),
                        tuple.getT2(),
                        session.machineId(),
                        tuple.getT3(),
                        session.shiftId(),
                        tuple.getT4(),
                        session.loginTime(),
                        session.logoutTime(),
                        session.status()));
    }
}
