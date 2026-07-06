package com.touchmind.work.flow.one.sample.repository;

import com.touchmind.work.flow.one.sample.model.MachineProfile;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MachineProfileRepository extends ReactiveMongoRepository<MachineProfile, String> {

    Mono<MachineProfile> findByMachineId(String machineId);
}
