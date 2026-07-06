package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.manufacturing.dto.DashboardSummaryResponse;
import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import com.touchmind.work.flow.one.manufacturing.enums.TestStatus;
import com.touchmind.work.flow.one.manufacturing.repository.MachineRepository;
import com.touchmind.work.flow.one.manufacturing.repository.PartRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DashboardService {

    private final MachineRepository machineRepository;
    private final PartRepository partRepository;

    public DashboardService(MachineRepository machineRepository, PartRepository partRepository) {
        this.machineRepository = machineRepository;
        this.partRepository = partRepository;
    }

    public Mono<DashboardSummaryResponse> summary() {
        return Mono.zip(
                machineRepository.findByStatus(MachineStatus.RUNNING).count(),
                machineRepository.findByStatus(MachineStatus.IDLE).count(),
                machineRepository.findByStatus(MachineStatus.ERROR).count(),
                partRepository.findByStatus(PartStatus.IN_PROCESS).count(),
                partRepository.findAll().filter(part -> part.testStatus() == TestStatus.PASS).count(),
                partRepository.findAll().filter(part -> part.status() == PartStatus.FAILED || part.status() == PartStatus.REWORK_REQUIRED).count(),
                partRepository.findByStatus(PartStatus.READY_FOR_NEXT_PHASE).count())
                .map(tuple -> new DashboardSummaryResponse(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        tuple.getT5(),
                        tuple.getT6(),
                        tuple.getT7()));
    }
}
