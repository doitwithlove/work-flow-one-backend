package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.MachineEventRequest;
import com.touchmind.work.flow.one.manufacturing.dto.MachineEventResponse;
import jakarta.validation.Valid;
import org.apache.camel.ProducerTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/machine-events")
public class MachineEventController {

    private final ProducerTemplate producerTemplate;

    public MachineEventController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @PostMapping
    public Mono<MachineEventResponse> ingest(@Valid @RequestBody MachineEventRequest request) {
        return Mono.fromCallable(() -> producerTemplate.requestBody("direct:manufacturing-machine-event", request, MachineEventResponse.class));
    }

    @GetMapping
    public Flux<MachineEventResponse> list() {
        return Mono.fromCallable(() -> producerTemplate.requestBody("direct:manufacturing-machine-event-list", null, List.class))
                .flatMapMany(Flux::fromIterable);
    }

    @GetMapping("/part/{partId}")
    public Flux<MachineEventResponse> listByPart(@PathVariable String partId) {
        return Mono.fromCallable(() -> producerTemplate.requestBody("direct:manufacturing-machine-event-part-list", partId, List.class))
                .flatMapMany(Flux::fromIterable);
    }
}
