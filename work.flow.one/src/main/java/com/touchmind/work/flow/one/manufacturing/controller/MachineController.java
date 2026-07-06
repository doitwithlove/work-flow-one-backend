package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.CreateMachineRequest;
import com.touchmind.work.flow.one.manufacturing.dto.MachineResponse;
import com.touchmind.work.flow.one.manufacturing.dto.UpdateMachineStatusRequest;
import com.touchmind.work.flow.one.manufacturing.service.MachineService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @PostMapping
    public Mono<MachineResponse> create(@Valid @RequestBody CreateMachineRequest request) {
        return machineService.create(request);
    }

    @GetMapping
    public Flux<MachineResponse> list() {
        return machineService.list();
    }

    @GetMapping("/{id}")
    public Mono<MachineResponse> get(@PathVariable String id) {
        return machineService.get(id);
    }

    @PutMapping("/{id}/status")
    public Mono<MachineResponse> updateStatus(@PathVariable String id, @Valid @RequestBody UpdateMachineStatusRequest request) {
        return machineService.updateStatus(id, request);
    }
}
