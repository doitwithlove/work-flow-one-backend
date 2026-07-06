package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.Part;
import com.touchmind.work.flow.one.manufacturing.enums.PartStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PartRepository extends ReactiveMongoRepository<Part, String> {

    Flux<Part> findByStatus(PartStatus status);

    Flux<Part> findByCurrentMachineId(String machineId);

    Flux<Part> findByPartNumberContainingIgnoreCase(String partNumber);

    Flux<Part> findByCurrentMachineIdAndStatus(String machineId, PartStatus status);
}
