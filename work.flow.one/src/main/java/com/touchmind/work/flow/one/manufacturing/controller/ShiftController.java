package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.CreateShiftRequest;
import com.touchmind.work.flow.one.manufacturing.dto.ShiftResponse;
import com.touchmind.work.flow.one.manufacturing.service.ShiftService;
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
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    public Mono<ShiftResponse> create(@Valid @RequestBody CreateShiftRequest request) {
        return shiftService.create(request);
    }

    @GetMapping
    public Flux<ShiftResponse> list() {
        return shiftService.list();
    }

    @GetMapping("/{id}")
    public Mono<ShiftResponse> get(@PathVariable String id) {
        return shiftService.get(id);
    }
}
