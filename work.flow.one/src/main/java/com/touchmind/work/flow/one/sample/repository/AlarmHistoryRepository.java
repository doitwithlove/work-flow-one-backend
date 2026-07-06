package com.touchmind.work.flow.one.sample.repository;

import com.touchmind.work.flow.one.sample.model.AlarmHistory;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface AlarmHistoryRepository extends ReactiveMongoRepository<AlarmHistory, String> {

    Flux<AlarmHistory> findByMachineIdOrderByTimestampDesc(String machineId);

    Flux<AlarmHistory> findByMachineIdAndTimestampBetweenOrderByTimestampDesc(String machineId, Instant from, Instant to);
}
