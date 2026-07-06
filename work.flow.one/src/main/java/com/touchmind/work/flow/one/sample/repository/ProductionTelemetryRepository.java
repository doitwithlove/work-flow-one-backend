package com.touchmind.work.flow.one.sample.repository;

import com.touchmind.work.flow.one.sample.model.ProductionTelemetry;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface ProductionTelemetryRepository extends ReactiveMongoRepository<ProductionTelemetry, String> {

    Flux<ProductionTelemetry> findByMachineIdOrderByTimestampDesc(String machineId);

    Flux<ProductionTelemetry> findByMachineIdAndTimestampBetweenOrderByTimestampDesc(String machineId, Instant from, Instant to);
}
