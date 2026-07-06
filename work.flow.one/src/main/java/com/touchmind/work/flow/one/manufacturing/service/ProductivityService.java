package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.manufacturing.domain.MachineEvent;
import com.touchmind.work.flow.one.manufacturing.domain.OperatorMachineSession;
import com.touchmind.work.flow.one.manufacturing.domain.Shift;
import com.touchmind.work.flow.one.manufacturing.domain.TestResult;
import com.touchmind.work.flow.one.manufacturing.dto.ProductivitySummaryResponse;
import com.touchmind.work.flow.one.manufacturing.enums.TestResultStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineEventRepository;
import com.touchmind.work.flow.one.manufacturing.repository.MachineRepository;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorMachineSessionRepository;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorRepository;
import com.touchmind.work.flow.one.manufacturing.repository.PartRepository;
import com.touchmind.work.flow.one.manufacturing.repository.ShiftRepository;
import com.touchmind.work.flow.one.manufacturing.repository.TestResultRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class ProductivityService {

    private final OperatorMachineSessionRepository sessionRepository;
    private final OperatorRepository operatorRepository;
    private final ShiftRepository shiftRepository;
    private final MachineRepository machineRepository;
    private final MachineEventRepository machineEventRepository;
    private final TestResultRepository testResultRepository;
    private final PartRepository partRepository;

    public ProductivityService(
            OperatorMachineSessionRepository sessionRepository,
            OperatorRepository operatorRepository,
            ShiftRepository shiftRepository,
            MachineRepository machineRepository,
            MachineEventRepository machineEventRepository,
            TestResultRepository testResultRepository,
            PartRepository partRepository) {
        this.sessionRepository = sessionRepository;
        this.operatorRepository = operatorRepository;
        this.shiftRepository = shiftRepository;
        this.machineRepository = machineRepository;
        this.machineEventRepository = machineEventRepository;
        this.testResultRepository = testResultRepository;
        this.partRepository = partRepository;
    }

    public Flux<ProductivitySummaryResponse> summary() {
        return sessionRepository.findAll().concatMap(this::summarizeSession);
    }

    public Flux<ProductivitySummaryResponse> byOperator(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            return Flux.empty();
        }
        return sessionRepository.findByOperatorIdOrderByLoginTimeDesc(operatorId.trim()).concatMap(this::summarizeSession);
    }

    public Flux<ProductivitySummaryResponse> byShift(String shiftId) {
        if (shiftId == null || shiftId.isBlank()) {
            return Flux.empty();
        }
        return sessionRepository.findAll()
                .filter(session -> shiftId.trim().equals(session.shiftId()))
                .concatMap(this::summarizeSession);
    }

    public Flux<ProductivitySummaryResponse> byMachine(String machineId) {
        if (machineId == null || machineId.isBlank()) {
            return Flux.empty();
        }
        return sessionRepository.findByMachineIdOrderByLoginTimeDesc(machineId.trim()).concatMap(this::summarizeSession);
    }

    public Mono<Void> recordTestResult(TestResult result) {
        return machineEventRepository.findFirstByPartIdOrderByReceivedAtDesc(result.partId()).then();
    }

    private Mono<ProductivitySummaryResponse> summarizeSession(OperatorMachineSession session) {
        Mono<String> operatorName = operatorRepository.findById(session.operatorId())
                .map(operator -> operator.firstName() + " " + operator.lastName())
                .defaultIfEmpty(session.operatorId());

        Mono<String> employeeCode = operatorRepository.findById(session.operatorId())
                .map(operator -> operator.employeeCode())
                .defaultIfEmpty(session.operatorId());

        Mono<Shift> shiftMono = shiftRepository.findById(session.shiftId());
        Mono<String> shiftName = shiftMono.map(Shift::name).defaultIfEmpty(session.shiftId());
        Mono<Integer> targetOutput = shiftMono.map(shift -> shift.targetOutput() == null ? 0 : shift.targetOutput()).defaultIfEmpty(0);

        Mono<String> machineName = machineRepository.findByMachineCode(session.machineId())
                .map(machine -> machine.name())
                .defaultIfEmpty(session.machineId());

        Mono<List<MachineEvent>> eventsMono = machineEventRepository.findByOperatorSessionIdOrderByReceivedAtDesc(session.id()).collectList();
        Mono<List<String>> partIdsMono = eventsMono.map(events -> events.stream()
                .map(MachineEvent::partId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        Mono<List<TestResult>> latestResultsMono = partIdsMono.flatMapMany(Flux::fromIterable)
                .flatMap(partId -> testResultRepository.findFirstByPartIdOrderByTestedAtDesc(partId).flux())
                .collectList();

        Mono<Integer> runtimeMinutesMono = Mono.fromSupplier(() -> sessionDurationMinutes(session));
        Mono<Integer> downtimeMinutesMono = shiftMono.map(this::plannedMinutes)
                .defaultIfEmpty(0)
                .zipWith(runtimeMinutesMono, (plannedMinutes, runtimeMinutes) -> Math.max(0, plannedMinutes - runtimeMinutes));

        return Mono.zip(operatorName, employeeCode, shiftName, targetOutput, machineName, partIdsMono, latestResultsMono, runtimeMinutesMono)
                .flatMap(tuple -> downtimeMinutesMono.map(downtimeMinutes -> {
                    int totalPartsProcessed = tuple.getT6().size();
                    int passedParts = 0;
                    int failedParts = 0;
                    int reworkParts = 0;

                    for (TestResult result : tuple.getT7()) {
                        if (result.result() == TestResultStatus.PASS) {
                            passedParts++;
                        } else {
                            double actual = result.actualValue() == null ? 0.0 : result.actualValue();
                            double expected = result.expectedValue() == null ? 0.0 : result.expectedValue();
                            double tolerance = Math.max(
                                    Math.abs(result.toleranceMin() == null ? 0.0 : result.toleranceMin()),
                                    Math.abs(result.toleranceMax() == null ? 0.0 : result.toleranceMax()));
                            if (Math.abs(actual - expected) > tolerance) {
                                failedParts++;
                            } else {
                                reworkParts++;
                            }
                        }
                    }

                    double qualityRate = safeDivide(passedParts, totalPartsProcessed);
                    double productionRate = safeDivide(totalPartsProcessed, tuple.getT4());
                    double machineUtilization = safeDivide(tuple.getT8(), downtimeMinutes + tuple.getT8());
                    double productivityScore = (qualityRate * 0.5) + (productionRate * 0.3) + (machineUtilization * 0.2);

                    return new ProductivitySummaryResponse(
                            session.operatorId(),
                            tuple.getT1(),
                            tuple.getT2(),
                            session.shiftId(),
                            tuple.getT3(),
                            session.machineId(),
                            tuple.getT5(),
                            totalPartsProcessed,
                            passedParts,
                            failedParts,
                            reworkParts,
                            tuple.getT8(),
                            downtimeMinutes,
                            qualityRate,
                            productionRate,
                            machineUtilization,
                            productivityScore,
                            Instant.now());
                }));
    }

    private int sessionDurationMinutes(OperatorMachineSession session) {
        Instant end = session.logoutTime() == null ? Instant.now() : session.logoutTime();
        return (int) Math.max(0, Duration.between(session.loginTime(), end).toMinutes());
    }

    private int plannedMinutes(Shift shift) {
        if (shift == null || shift.startTime() == null || shift.endTime() == null) {
            return 0;
        }

        LocalTime start = shift.startTime();
        LocalTime end = shift.endTime();
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < 0) {
            minutes += Duration.ofDays(1).toMinutes();
        }
        return (int) minutes;
    }

    private double safeDivide(double numerator, double denominator) {
        return denominator <= 0.0 ? 0.0 : numerator / denominator;
    }
}
