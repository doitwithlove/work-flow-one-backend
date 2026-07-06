package com.touchmind.work.flow.one.manufacturing.config;

import com.touchmind.work.flow.one.manufacturing.domain.Machine;
import com.touchmind.work.flow.one.manufacturing.domain.MachineEvent;
import com.touchmind.work.flow.one.manufacturing.domain.Operator;
import com.touchmind.work.flow.one.manufacturing.domain.OperatorMachineSession;
import com.touchmind.work.flow.one.manufacturing.domain.Part;
import com.touchmind.work.flow.one.manufacturing.domain.ProcessStep;
import com.touchmind.work.flow.one.manufacturing.domain.Shift;
import com.touchmind.work.flow.one.manufacturing.domain.TestResult;
import com.touchmind.work.flow.one.manufacturing.enums.MachineEventType;
import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import com.touchmind.work.flow.one.manufacturing.enums.OperatorSessionStatus;
import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestResultStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineEventRepository;
import com.touchmind.work.flow.one.manufacturing.repository.MachineRepository;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorMachineSessionRepository;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorRepository;
import com.touchmind.work.flow.one.manufacturing.repository.PartRepository;
import com.touchmind.work.flow.one.manufacturing.repository.ProcessStepRepository;
import com.touchmind.work.flow.one.manufacturing.repository.ShiftRepository;
import com.touchmind.work.flow.one.manufacturing.repository.TestResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ManufacturingSeedDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ManufacturingSeedDataSeeder.class);

    private final ManufacturingSeedDataProperties properties;
    private final MachineRepository machineRepository;
    private final OperatorRepository operatorRepository;
    private final ShiftRepository shiftRepository;
    private final OperatorMachineSessionRepository sessionRepository;
    private final ProcessStepRepository processStepRepository;
    private final PartRepository partRepository;
    private final MachineEventRepository machineEventRepository;
    private final TestResultRepository testResultRepository;

    public ManufacturingSeedDataSeeder(
            ManufacturingSeedDataProperties properties,
            MachineRepository machineRepository,
            OperatorRepository operatorRepository,
            ShiftRepository shiftRepository,
            OperatorMachineSessionRepository sessionRepository,
            ProcessStepRepository processStepRepository,
            PartRepository partRepository,
            MachineEventRepository machineEventRepository,
            TestResultRepository testResultRepository) {
        this.properties = properties;
        this.machineRepository = machineRepository;
        this.operatorRepository = operatorRepository;
        this.shiftRepository = shiftRepository;
        this.sessionRepository = sessionRepository;
        this.processStepRepository = processStepRepository;
        this.partRepository = partRepository;
        this.machineEventRepository = machineEventRepository;
        this.testResultRepository = testResultRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        seed().block();
        log.info("Manufacturing sample data seeded.");
    }

    private Mono<Void> seed() {
        return ensureOperationalBackfill()
                .then(partRepository.count().flatMap(count -> count > 0 ? Mono.empty() : ensureCoreManufacturingData()));
    }

    private Mono<Void> ensureOperationalBackfill() {
        Mono<Void> machineSeed = machineRepository.findByMachineCode("CNC-01")
                .switchIfEmpty(machineRepository.save(new Machine(null, "CNC-01", "CNC Milling Line 1", "CNC", MachineStatus.RUNNING, Instant.now())))
                .then(machineRepository.findByMachineCode("QC-01")
                        .switchIfEmpty(machineRepository.save(new Machine(null, "QC-01", "Inspection Station", "QC", MachineStatus.IDLE, Instant.now()))))
                .then();

        Mono<Void> operatorSeed = operatorRepository.findByEmployeeCode("OPR-1001")
                .switchIfEmpty(operatorRepository.save(new Operator(null, "OPR-1001", "Mika", "Santos", "OPERATOR", "ADVANCED", Boolean.TRUE, Instant.now(), Instant.now())))
                .then();

        Mono<Void> shiftSeed = shiftRepository.findByName("DAY")
                .switchIfEmpty(shiftRepository.save(new Shift(null, "DAY", LocalTime.of(6, 0), LocalTime.of(14, 0), 80, Boolean.TRUE)))
                .then();

        Mono<Void> sessionSeed = machineRepository.findByMachineCode("CNC-01")
                .flatMap(machine -> operatorRepository.findByEmployeeCode("OPR-1001")
                        .flatMap(operator -> shiftRepository.findByName("DAY")
                                .flatMap(shift -> sessionRepository.findFirstByOperatorIdAndStatus(operator.id(), OperatorSessionStatus.ACTIVE)
                                        .switchIfEmpty(sessionRepository.save(new OperatorMachineSession(null, operator.id(), machine.machineCode(), shift.id(), Instant.now().minusSeconds(7200), null, OperatorSessionStatus.ACTIVE)))
                                        .then())))
                .then();

        return machineSeed.then(operatorSeed).then(shiftSeed).then(sessionSeed);
    }

    private Mono<Void> ensureCoreManufacturingData() {
        return processStepRepository.save(new ProcessStep(null, 30, "Packaging", "PACK", "VISUAL", null))
                .flatMap(saved30 -> processStepRepository.save(new ProcessStep(null, 20, "Inspection", "QC", "DIMENSION", saved30.id()))
                        .flatMap(saved20 -> processStepRepository.save(new ProcessStep(null, 10, "Cutting", "CNC", "DIMENSION", saved20.id()))
                                .flatMap(saved10 -> {
                    Machine m1 = new Machine(null, "CNC-01", "CNC Milling Line 1", "CNC", MachineStatus.RUNNING, Instant.now());
                    Machine m2 = new Machine(null, "QC-01", "Inspection Station", "QC", MachineStatus.IDLE, Instant.now());
                    return machineRepository.save(m1)
                            .flatMap(savedMachine -> operatorRepository.findByEmployeeCode("OPR-1001")
                                    .flatMap(operator -> shiftRepository.findByName("DAY")
                                            .flatMap(shift -> sessionRepository.save(new OperatorMachineSession(null, operator.id(), savedMachine.machineCode(), shift.id(), Instant.now().minusSeconds(7200), null, OperatorSessionStatus.ACTIVE)))))
                            .then(machineRepository.save(m2))
                            .then(partRepository.save(new Part(null, "PART-10001", "BATCH-A1", saved10.id(), "CNC-01", PartStatus.IN_PROCESS, TestStatus.PENDING, Instant.now(), Instant.now())))
                            .then(partRepository.save(new Part(null, "PART-10002", "BATCH-A1", saved20.id(), "QC-01", PartStatus.READY_FOR_NEXT_PHASE, TestStatus.PASS, Instant.now(), Instant.now())))
                            .then(partRepository.save(new Part(null, "PART-10003", "BATCH-A2", saved10.id(), "CNC-01", PartStatus.TEST_PENDING, TestStatus.PENDING, Instant.now(), Instant.now())))
                            .then(operatorRepository.findByEmployeeCode("OPR-1001")
                                    .flatMap(operator -> machineEventRepository.save(new MachineEvent(null, "CNC-01", "PART-10001", operator.id(), null, MachineEventType.PROCESS_STARTED, "RUNNING", buildPayload(65, 1500, 420), Instant.now()))))
                            .then(operatorRepository.findByEmployeeCode("OPR-1001")
                                    .flatMap(operator -> machineEventRepository.save(new MachineEvent(null, "QC-01", "PART-10002", operator.id(), null, MachineEventType.PROCESS_COMPLETED, "COMPLETED", buildPayload(42, 0, 180), Instant.now()))))
                            .then(testResultRepository.save(new TestResult(null, "PART-10002", "QC-01", "DIMENSION", 10.0, 10.0, -0.05, 0.05, TestResultStatus.PASS, Instant.now())))
                            .then(testResultRepository.save(new TestResult(null, "PART-10003", "QC-01", "DIMENSION", 12.0, 12.2, -0.05, 0.05, TestResultStatus.FAIL, Instant.now())))
                            .then();
                })));
    }

    private Map<String, Object> buildPayload(double temperature, double speed, double durationSeconds) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("temperature", temperature);
        payload.put("speed", speed);
        payload.put("durationSeconds", durationSeconds);
        return payload;
    }
}
