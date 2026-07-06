package com.touchmind.work.flow.one.sample.repository;

import com.touchmind.work.flow.one.sample.model.OeeShiftSummary;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface OeeShiftSummaryRepository extends ReactiveMongoRepository<OeeShiftSummary, String> {

    Flux<OeeShiftSummary> findByMachineIdOrderByDateDesc(String machineId);

    Flux<OeeShiftSummary> findByMachineIdAndDateBetweenOrderByDateDesc(String machineId, LocalDate from, LocalDate to);
}
