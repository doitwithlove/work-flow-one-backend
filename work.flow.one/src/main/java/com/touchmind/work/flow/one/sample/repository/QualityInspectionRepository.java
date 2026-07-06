package com.touchmind.work.flow.one.sample.repository;

import com.touchmind.work.flow.one.sample.model.QualityInspection;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface QualityInspectionRepository extends ReactiveMongoRepository<QualityInspection, String> {

    Flux<QualityInspection> findByMachineIdOrderByTimestampDesc(String machineId);

    Flux<QualityInspection> findByMachineIdAndTimestampBetweenOrderByTimestampDesc(String machineId, Instant from, Instant to);
}
