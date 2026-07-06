package com.touchmind.work.flow.one.superuser.service;

import com.touchmind.work.flow.one.dto.UserResponse;
import com.touchmind.work.flow.one.manufacturing.domain.Machine;
import com.touchmind.work.flow.one.manufacturing.domain.OperatorMachineSession;
import com.touchmind.work.flow.one.manufacturing.domain.Part;
import com.touchmind.work.flow.one.manufacturing.domain.ProcessStep;
import com.touchmind.work.flow.one.manufacturing.domain.TestResult;
import com.touchmind.work.flow.one.manufacturing.dto.OperatorMachineSessionResponse;
import com.touchmind.work.flow.one.manufacturing.dto.PartResponse;
import com.touchmind.work.flow.one.manufacturing.dto.ProductivitySummaryResponse;
import com.touchmind.work.flow.one.manufacturing.dto.TestResultResponse;
import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineRepository;
import com.touchmind.work.flow.one.manufacturing.repository.OperatorMachineSessionRepository;
import com.touchmind.work.flow.one.manufacturing.repository.PartRepository;
import com.touchmind.work.flow.one.manufacturing.repository.ProcessStepRepository;
import com.touchmind.work.flow.one.manufacturing.repository.TestResultRepository;
import com.touchmind.work.flow.one.manufacturing.service.OperatorSessionService;
import com.touchmind.work.flow.one.manufacturing.service.ProductivityService;
import com.touchmind.work.flow.one.model.UserRole;
import com.touchmind.work.flow.one.repository.UserRepository;
import com.touchmind.work.flow.one.superuser.dto.MachineProgressResponse;
import com.touchmind.work.flow.one.superuser.dto.ProcessStepProgressResponse;
import com.touchmind.work.flow.one.superuser.dto.SuperUserDashboardResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuperUserDashboardService {

    private final MachineRepository machineRepository;
    private final PartRepository partRepository;
    private final ProcessStepRepository processStepRepository;
    private final OperatorMachineSessionRepository operatorSessionRepository;
    private final TestResultRepository testResultRepository;
    private final ProductivityService productivityService;
    private final OperatorSessionService operatorSessionService;
    private final UserRepository userRepository;

    public SuperUserDashboardService(
            MachineRepository machineRepository,
            PartRepository partRepository,
            ProcessStepRepository processStepRepository,
            OperatorMachineSessionRepository operatorSessionRepository,
            TestResultRepository testResultRepository,
            ProductivityService productivityService,
            OperatorSessionService operatorSessionService,
            UserRepository userRepository) {
        this.machineRepository = machineRepository;
        this.partRepository = partRepository;
        this.processStepRepository = processStepRepository;
        this.operatorSessionRepository = operatorSessionRepository;
        this.testResultRepository = testResultRepository;
        this.productivityService = productivityService;
        this.operatorSessionService = operatorSessionService;
        this.userRepository = userRepository;
    }

    public Mono<SuperUserDashboardResponse> dashboard() {
        Set<String> operatorAuthorities = Set.of(
                UserRole.SUPER_USER.authority(),
                UserRole.MANAGER.authority(),
                UserRole.SUPERVISOR.authority(),
                UserRole.OPERATOR.authority(),
                UserRole.QUALITY_INSPECTOR.authority());

        Mono<List<Machine>> machinesMono = machineRepository.findAll().collectList();
        Mono<List<Part>> partsMono = partRepository.findAll().collectList();
        Mono<List<ProcessStep>> stepsMono = processStepRepository.findAllByOrderByStepNumberAsc().collectList();
        Mono<List<OperatorMachineSession>> sessionsMono = operatorSessionRepository.findByStatusOrderByLoginTimeDesc(com.touchmind.work.flow.one.manufacturing.enums.OperatorSessionStatus.ACTIVE).collectList();
        Mono<List<TestResult>> testResultsMono = testResultRepository.findAll()
                .sort(Comparator.comparing(TestResult::testedAt).reversed())
                .collectList();
        Mono<List<UserResponse>> operatorsMono = userRepository.findAll()
                .filter(user -> user.getRoles().stream()
                        .map(UserRole::fromValue)
                        .flatMap(Optional::stream)
                        .map(UserRole::authority)
                        .anyMatch(operatorAuthorities::contains))
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRoles(),
                        user.isEnabled(),
                        user.isActive(),
                        user.getCreatedAt(),
                        user.getPhoneNumber(),
                        user.getBirthday(),
                        user.getPosition(),
                        user.getProfilePictureUrl(),
                        user.getSocialContacts()))
                .collectList();

        Mono<List<ProductivitySummaryResponse>> productivityMono = productivityService.summary().collectList();

        return Mono.zip(machinesMono, partsMono, stepsMono, sessionsMono, testResultsMono, productivityMono, operatorsMono)
                .map(tuple -> {
                    List<Machine> machines = tuple.getT1();
                    List<Part> parts = tuple.getT2();
                    List<ProcessStep> steps = tuple.getT3();
                    List<OperatorMachineSession> sessions = tuple.getT4();
                    List<TestResult> testResults = tuple.getT5();
                    List<ProductivitySummaryResponse> productivity = tuple.getT6();
                    List<UserResponse> operators = tuple.getT7();

                    Map<String, UserResponse> operatorById = operators.stream().collect(Collectors.toMap(UserResponse::id, operator -> operator));
                    Map<String, Machine> machineByCode = machines.stream().collect(Collectors.toMap(Machine::machineCode, machine -> machine, (first, second) -> first));
                    Map<String, Machine> machineById = machines.stream().collect(Collectors.toMap(Machine::id, machine -> machine, (first, second) -> first));
                    Map<String, ProcessStep> stepById = steps.stream().collect(Collectors.toMap(ProcessStep::id, step -> step));
                    int maxStepNumber = steps.stream().map(ProcessStep::stepNumber).max(Integer::compareTo).orElse(1);

                    long runningMachines = machines.stream().filter(machine -> machine.status() == MachineStatus.RUNNING).count();
                    long idleMachines = machines.stream().filter(machine -> machine.status() == MachineStatus.IDLE).count();
                    long errorMachines = machines.stream().filter(machine -> machine.status() == MachineStatus.ERROR).count();
                    long stoppedMachines = machines.stream().filter(machine -> machine.status() == MachineStatus.STOPPED).count();

                    long totalParts = parts.size();
                    long partsInProcess = parts.stream().filter(part -> part.status() == PartStatus.IN_PROCESS).count();
                    long partsWaitingForTest = parts.stream().filter(part -> part.status() == PartStatus.TEST_PENDING || part.testStatus() == TestStatus.PENDING).count();
                    long partsPassed = parts.stream().filter(part -> part.testStatus() == TestStatus.PASS || part.status() == PartStatus.PASSED).count();
                    long partsFailed = parts.stream().filter(part -> part.status() == PartStatus.FAILED || part.status() == PartStatus.REWORK_REQUIRED).count();
                    long partsReadyForNextPhase = parts.stream().filter(part -> part.status() == PartStatus.READY_FOR_NEXT_PHASE).count();

                    long activeOperators = sessions.stream().map(OperatorMachineSession::operatorId).filter(Objects::nonNull).distinct().count();

                    List<MachineProgressResponse> machineProgressList = machines.stream()
                            .map(machine -> buildMachineProgress(machine, parts, sessions, stepById, operatorById, maxStepNumber))
                            .toList();

                    List<ProcessStepProgressResponse> processStepProgressList = steps.stream()
                            .map(step -> buildStepProgress(step, parts, maxStepNumber))
                            .toList();

                    List<OperatorMachineSessionResponse> activeSessionList = sessions.stream()
                            .map(session -> {
                                UserResponse operator = operatorById.get(session.operatorId());
                                Machine machine = machineByCode.getOrDefault(session.machineId(), machineById.get(session.machineId()));
                                String operatorName = operator == null ? session.operatorId() : operator.fullName() != null && !operator.fullName().isBlank() ? operator.fullName() : operator.username();
                                String employeeCode = operator == null ? session.operatorId() : operator.username();
                                String machineName = machine == null ? session.machineId() : machine.name();
                                return new OperatorMachineSessionResponse(
                                        session.id(),
                                        session.operatorId(),
                                        operatorName,
                                        employeeCode,
                                        session.machineId(),
                                        machineName,
                                        session.shiftId(),
                                        session.shiftId(),
                                        session.loginTime(),
                                        session.logoutTime(),
                                        session.status());
                            })
                            .toList();

                    List<TestResultResponse> recentTestResultResponses = testResults.stream()
                            .limit(25)
                            .map(result -> new TestResultResponse(
                                    result.id(),
                                    result.partId(),
                                    result.machineId(),
                                    result.testType(),
                                    result.expectedValue(),
                                    result.actualValue(),
                                    result.toleranceMin(),
                                    result.toleranceMax(),
                                    result.result(),
                                    result.testedAt()))
                            .toList();

                    List<PartResponse> failedPartsList = parts.stream()
                            .filter(part -> part.status() == PartStatus.FAILED || part.status() == PartStatus.REWORK_REQUIRED)
                            .map(this::toPartResponse)
                            .toList();

                    List<PartResponse> waitingForTestList = parts.stream()
                            .filter(part -> part.status() == PartStatus.TEST_PENDING || part.testStatus() == TestStatus.PENDING)
                            .map(this::toPartResponse)
                            .toList();

                    return new SuperUserDashboardResponse(
                            machines.size(),
                            runningMachines,
                            idleMachines,
                            errorMachines,
                            stoppedMachines,
                            totalParts,
                            partsInProcess,
                            partsWaitingForTest,
                            partsPassed,
                            partsFailed,
                            partsReadyForNextPhase,
                            activeOperators,
                            sessions.size(),
                            productivity,
                            machineProgressList,
                            processStepProgressList,
                            activeSessionList,
                            recentTestResultResponses,
                            failedPartsList,
                            waitingForTestList,
                            operators);
                });
    }

    private MachineProgressResponse buildMachineProgress(
            Machine machine,
            List<Part> parts,
            List<OperatorMachineSession> sessions,
            Map<String, ProcessStep> stepById,
            Map<String, UserResponse> operatorById,
            int maxStepNumber) {
        Part currentPart = parts.stream()
                .filter(part -> machine.machineCode().equals(part.currentMachineId()) || machine.id().equals(part.currentMachineId()))
                .sorted(Comparator.comparing(Part::updatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .findFirst()
                .orElse(null);

        OperatorMachineSession activeSession = sessions.stream()
                .filter(session -> machine.machineCode().equals(session.machineId()) || machine.id().equals(session.machineId()))
                .findFirst()
                .orElse(null);

        ProcessStep currentStep = currentPart == null ? null : stepById.get(currentPart.currentStepId());
        double progress = currentStep == null ? 0.0 : Math.min(100.0, (currentStep.stepNumber() * 100.0) / Math.max(1, maxStepNumber));

        return new MachineProgressResponse(
                machine.id(),
                machine.machineCode(),
                machine.name(),
                machine.status(),
                currentPart == null ? null : currentPart.id(),
                currentPart == null ? null : currentPart.partNumber(),
                activeSession == null ? null : activeSession.operatorId(),
                activeSession == null ? null : resolveOperatorName(activeSession.operatorId(), operatorById),
                currentStep == null ? null : currentStep.name(),
                progress,
                machine.lastSignalAt());
    }

    private ProcessStepProgressResponse buildStepProgress(ProcessStep step, List<Part> parts, int maxStepNumber) {
        List<Part> stepParts = parts.stream()
                .filter(part -> step.id().equals(part.currentStepId()))
                .toList();

        long completedParts = stepParts.stream().filter(part -> part.status() == PartStatus.READY_FOR_NEXT_PHASE || part.status() == PartStatus.COMPLETED || part.testStatus() == TestStatus.PASS).count();
        long inProcessParts = stepParts.stream().filter(part -> part.status() == PartStatus.IN_PROCESS).count();
        long failedParts = stepParts.stream().filter(part -> part.status() == PartStatus.FAILED || part.status() == PartStatus.REWORK_REQUIRED).count();
        long waitingParts = stepParts.stream().filter(part -> part.status() == PartStatus.WAITING || part.status() == PartStatus.TEST_PENDING || part.testStatus() == TestStatus.PENDING).count();
        long totalParts = stepParts.size();
        double progress = totalParts == 0 ? 0.0 : Math.min(100.0, (completedParts * 100.0) / totalParts);

        return new ProcessStepProgressResponse(
                step.id(),
                step.stepNumber(),
                step.name(),
                totalParts,
                completedParts,
                inProcessParts,
                failedParts,
                waitingParts,
                progress);
    }

    private PartResponse toPartResponse(Part part) {
        return new PartResponse(
                part.id(),
                part.partNumber(),
                part.batchNumber(),
                part.currentStepId(),
                part.currentMachineId(),
                part.status(),
                part.testStatus(),
                part.createdAt(),
                part.updatedAt());
    }

    private String resolveOperatorName(String operatorId, Map<String, UserResponse> operatorById) {
        UserResponse operator = operatorById.get(operatorId);
        if (operator == null) {
            return operatorId;
        }

        if (operator.fullName() != null && !operator.fullName().isBlank()) {
            return operator.fullName();
        }

        return operator.username();
    }
}
