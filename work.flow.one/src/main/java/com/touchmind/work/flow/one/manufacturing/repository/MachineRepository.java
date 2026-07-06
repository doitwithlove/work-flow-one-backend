package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.Machine;
import com.touchmind.work.flow.one.manufacturing.enums.MachineStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MachineRepository extends ReactiveMongoRepository<Machine, String> {

    Mono<Machine> findByMachineCode(String machineCode);

    Flux<Machine> findByStatus(MachineStatus status);
}
