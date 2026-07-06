package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.CreatePartRequest;
import com.touchmind.work.flow.one.manufacturing.dto.PartHistoryResponse;
import com.touchmind.work.flow.one.manufacturing.dto.PartResponse;
import com.touchmind.work.flow.one.manufacturing.dto.UpdatePartStatusRequest;
import com.touchmind.work.flow.one.manufacturing.service.PartService;
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
@RequestMapping("/api/parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @PostMapping
    public Mono<PartResponse> create(@Valid @RequestBody CreatePartRequest request) {
        return partService.create(request);
    }

    @GetMapping
    public Flux<PartResponse> list() {
        return partService.list();
    }

    @GetMapping("/{id}")
    public Mono<PartResponse> get(@PathVariable String id) {
        return partService.get(id);
    }

    @GetMapping("/{id}/history")
    public Mono<PartHistoryResponse> history(@PathVariable String id) {
        return partService.history(id);
    }

    @PutMapping("/{id}/status")
    public Mono<PartResponse> updateStatus(@PathVariable String id, @Valid @RequestBody UpdatePartStatusRequest request) {
        return partService.updateStatus(id, request);
    }

    @PostMapping("/{partId}/move-next")
    public Mono<PartResponse> moveNext(@PathVariable String partId) {
        return partService.moveNext(partId);
    }
}
