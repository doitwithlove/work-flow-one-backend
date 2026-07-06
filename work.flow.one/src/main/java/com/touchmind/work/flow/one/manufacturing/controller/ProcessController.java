package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.CreateProcessStepRequest;
import com.touchmind.work.flow.one.manufacturing.dto.PartResponse;
import com.touchmind.work.flow.one.manufacturing.dto.ProcessStepResponse;
import com.touchmind.work.flow.one.manufacturing.service.PartService;
import com.touchmind.work.flow.one.manufacturing.service.ProcessStepService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/process-steps")
public class ProcessController {

    private final ProcessStepService processStepService;
    private final PartService partService;

    public ProcessController(ProcessStepService processStepService, PartService partService) {
        this.processStepService = processStepService;
        this.partService = partService;
    }

    @PostMapping
    public Mono<ProcessStepResponse> create(@Valid @RequestBody CreateProcessStepRequest request) {
        return processStepService.create(request);
    }

    @GetMapping
    public Flux<ProcessStepResponse> list() {
        return processStepService.list();
    }

    @PostMapping("/parts/{partId}/move-next")
    public Mono<PartResponse> moveNext(@PathVariable String partId) {
        return partService.moveNext(partId);
    }
}
