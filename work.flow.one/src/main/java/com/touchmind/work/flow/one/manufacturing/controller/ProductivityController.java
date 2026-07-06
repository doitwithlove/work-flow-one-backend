package com.touchmind.work.flow.one.manufacturing.controller;

import com.touchmind.work.flow.one.manufacturing.dto.ProductivitySummaryResponse;
import com.touchmind.work.flow.one.manufacturing.service.ProductivityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/productivity")
public class ProductivityController {

    private final ProductivityService productivityService;

    public ProductivityController(ProductivityService productivityService) {
        this.productivityService = productivityService;
    }

    @GetMapping("/summary")
    public Flux<ProductivitySummaryResponse> summary() {
        return productivityService.summary();
    }

    @GetMapping("/by-operator/{operatorId}")
    public Flux<ProductivitySummaryResponse> byOperator(@PathVariable String operatorId) {
        return productivityService.byOperator(operatorId);
    }

    @GetMapping("/by-shift/{shiftId}")
    public Flux<ProductivitySummaryResponse> byShift(@PathVariable String shiftId) {
        return productivityService.byShift(shiftId);
    }

    @GetMapping("/by-machine/{machineId}")
    public Flux<ProductivitySummaryResponse> byMachine(@PathVariable String machineId) {
        return productivityService.byMachine(machineId);
    }
}
