package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.EndOperatorSessionRequest;
import com.touchmind.work.flow.one.manufacturing.dto.OperatorMachineSessionResponse;
import com.touchmind.work.flow.one.manufacturing.dto.StartOperatorSessionRequest;
import com.touchmind.work.flow.one.manufacturing.service.OperatorSessionService;
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
@RequestMapping("/api/operator-sessions")
public class OperatorSessionController {

    private final OperatorSessionService sessionService;

    public OperatorSessionController(OperatorSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/start")
    public Mono<OperatorMachineSessionResponse> start(@Valid @RequestBody StartOperatorSessionRequest request) {
        return sessionService.startSession(request);
    }

    @PostMapping("/end")
    public Mono<OperatorMachineSessionResponse> end(@Valid @RequestBody EndOperatorSessionRequest request) {
        return sessionService.endSession(request);
    }

    @GetMapping("/active")
    public Flux<OperatorMachineSessionResponse> active() {
        return sessionService.activeSessions();
    }

    @GetMapping("/operator/{operatorId}")
    public Flux<OperatorMachineSessionResponse> byOperator(@PathVariable String operatorId) {
        return sessionService.sessionsByOperator(operatorId);
    }

    @GetMapping("/machine/{machineId}")
    public Flux<OperatorMachineSessionResponse> byMachine(@PathVariable String machineId) {
        return sessionService.sessionsByMachine(machineId);
    }
}
