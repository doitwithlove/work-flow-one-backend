package com.touchmind.work.flow.one.sample.controller;

import com.touchmind.work.flow.one.sample.model.AlarmHistory;
import com.touchmind.work.flow.one.sample.model.MachineProfile;
import com.touchmind.work.flow.one.sample.model.OeeShiftSummary;
import com.touchmind.work.flow.one.sample.model.ProductionTelemetry;
import com.touchmind.work.flow.one.sample.model.QualityInspection;
import com.touchmind.work.flow.one.sample.model.ToolWearPredictive;
import com.touchmind.work.flow.one.sample.repository.AlarmHistoryRepository;
import com.touchmind.work.flow.one.sample.repository.MachineProfileRepository;
import com.touchmind.work.flow.one.sample.repository.OeeShiftSummaryRepository;
import com.touchmind.work.flow.one.sample.repository.ProductionTelemetryRepository;
import com.touchmind.work.flow.one.sample.repository.QualityInspectionRepository;
import com.touchmind.work.flow.one.sample.repository.ToolWearPredictiveRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/sample")
public class IndustrialSampleController {

    private final ProductionTelemetryRepository telemetryRepository;
    private final AlarmHistoryRepository alarmHistoryRepository;
    private final MachineProfileRepository machineProfileRepository;
    private final OeeShiftSummaryRepository oeeShiftSummaryRepository;
    private final ToolWearPredictiveRepository toolWearPredictiveRepository;
    private final QualityInspectionRepository qualityInspectionRepository;

    public IndustrialSampleController(
            ProductionTelemetryRepository telemetryRepository,
            AlarmHistoryRepository alarmHistoryRepository,
            MachineProfileRepository machineProfileRepository,
            OeeShiftSummaryRepository oeeShiftSummaryRepository,
            ToolWearPredictiveRepository toolWearPredictiveRepository,
            QualityInspectionRepository qualityInspectionRepository) {
        this.telemetryRepository = telemetryRepository;
        this.alarmHistoryRepository = alarmHistoryRepository;
        this.machineProfileRepository = machineProfileRepository;
        this.oeeShiftSummaryRepository = oeeShiftSummaryRepository;
        this.toolWearPredictiveRepository = toolWearPredictiveRepository;
        this.qualityInspectionRepository = qualityInspectionRepository;
    }

    @GetMapping("/machine")
    public Mono<MachineProfile> machine(
            @RequestParam(defaultValue = "HURCO-M10-01") String machineId) {
        return machineProfileRepository.findByMachineId(machineId);
    }

    @GetMapping("/telemetry")
    public Flux<ProductionTelemetry> telemetry(
            @RequestParam(defaultValue = "HURCO-M10-01") String machineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "1000") int limit) {
        Flux<ProductionTelemetry> stream = from != null && to != null
                ? telemetryRepository.findByMachineIdAndTimestampBetweenOrderByTimestampDesc(machineId, from, to)
                : telemetryRepository.findByMachineIdOrderByTimestampDesc(machineId);
        return stream.take(limit);
    }

    @GetMapping("/alarms")
    public Flux<AlarmHistory> alarms(
            @RequestParam(defaultValue = "HURCO-M10-01") String machineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "500") int limit) {
        Flux<AlarmHistory> stream = from != null && to != null
                ? alarmHistoryRepository.findByMachineIdAndTimestampBetweenOrderByTimestampDesc(machineId, from, to)
                : alarmHistoryRepository.findByMachineIdOrderByTimestampDesc(machineId);
        return stream.take(limit);
    }

    @GetMapping("/oee")
    public Flux<OeeShiftSummary> oee(
            @RequestParam(defaultValue = "HURCO-M10-01") String machineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return from != null && to != null
                ? oeeShiftSummaryRepository.findByMachineIdAndDateBetweenOrderByDateDesc(machineId, from, to)
                : oeeShiftSummaryRepository.findByMachineIdOrderByDateDesc(machineId);
    }

    @GetMapping("/tool-wear")
    public Flux<ToolWearPredictive> toolWear(
            @RequestParam(defaultValue = "HURCO-M10-01") String machineId,
            @RequestParam(required = false) String tool) {
        return tool != null && !tool.isBlank()
                ? toolWearPredictiveRepository.findByMachineIdAndToolOrderByTimestampDesc(machineId, tool)
                : toolWearPredictiveRepository.findByMachineIdOrderByTimestampDesc(machineId);
    }

    @GetMapping("/quality")
    public Flux<QualityInspection> quality(
            @RequestParam(defaultValue = "HURCO-M10-01") String machineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "1000") int limit) {
        Flux<QualityInspection> stream = from != null && to != null
                ? qualityInspectionRepository.findByMachineIdAndTimestampBetweenOrderByTimestampDesc(machineId, from, to)
                : qualityInspectionRepository.findByMachineIdOrderByTimestampDesc(machineId);
        return stream.take(limit);
    }
}
