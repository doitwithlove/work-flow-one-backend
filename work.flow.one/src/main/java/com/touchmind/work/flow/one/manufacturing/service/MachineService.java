package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.manufacturing.domain.Machine;
import com.touchmind.work.flow.one.manufacturing.dto.CreateMachineRequest;
import com.touchmind.work.flow.one.manufacturing.dto.MachineResponse;
import com.touchmind.work.flow.one.manufacturing.dto.UpdateMachineStatusRequest;
import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final OperatorSessionService operatorSessionService;

    public MachineService(MachineRepository machineRepository, OperatorSessionService operatorSessionService) {
        this.machineRepository = machineRepository;
        this.operatorSessionService = operatorSessionService;
    }

    public Mono<MachineResponse> create(CreateMachineRequest request) {
        Instant now = Instant.now();
        Machine machine = new Machine(
                null,
                request.machineCode().trim(),
                request.name().trim(),
                request.type().trim(),
                request.status(),
                now);
        return machineRepository.save(machine).flatMap(this::toResponse);
    }

    public Flux<MachineResponse> list() {
        return machineRepository.findAll().flatMap(this::toResponse);
    }

    public Mono<MachineResponse> get(String id) {
        return machineRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Machine not found")))
                .flatMap(this::toResponse);
    }

    public Mono<MachineResponse> updateStatus(String id, UpdateMachineStatusRequest request) {
        return machineRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Machine not found")))
                .flatMap(machine -> machineRepository.save(new Machine(
                        machine.id(),
                        machine.machineCode(),
                        machine.name(),
                        machine.type(),
                        request.status(),
                        Instant.now())))
                .flatMap(this::toResponse);
    }

    public Mono<Machine> markStatus(String machineCode, MachineStatus status) {
        return machineRepository.findByMachineCode(machineCode)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Machine not found")))
                .flatMap(machine -> machineRepository.save(new Machine(
                        machine.id(),
                        machine.machineCode(),
                        machine.name(),
                        machine.type(),
                        status,
                        Instant.now())));
    }

    public Mono<Machine> findByMachineCode(String machineCode) {
        return machineRepository.findByMachineCode(machineCode)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Machine not found")));
    }

    private Mono<MachineResponse> toResponse(Machine machine) {
        if (machine.machineCode() == null || machine.machineCode().isBlank()) {
            return Mono.just(new MachineResponse(
                    machine.id(),
                    machine.machineCode(),
                    machine.name(),
                    machine.type(),
                    machine.status(),
                    machine.lastSignalAt(),
                    null,
                    null,
                    null));
        }

        return operatorSessionService.findActiveSessionResponseByMachine(machine.machineCode())
                .map(session -> new MachineResponse(
                        machine.id(),
                        machine.machineCode(),
                        machine.name(),
                        machine.type(),
                        machine.status(),
                        machine.lastSignalAt(),
                        session.operatorId(),
                        session.operatorName(),
                        session.loginTime()))
                .switchIfEmpty(Mono.just(new MachineResponse(
                        machine.id(),
                        machine.machineCode(),
                        machine.name(),
                        machine.type(),
                        machine.status(),
                        machine.lastSignalAt(),
                        null,
                        null,
                        null)));
    }
}
