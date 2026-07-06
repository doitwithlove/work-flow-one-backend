package com.touchmind.work.flow.one.sample.repository;

import com.touchmind.work.flow.one.sample.model.ToolWearPredictive;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ToolWearPredictiveRepository extends ReactiveMongoRepository<ToolWearPredictive, String> {

    Flux<ToolWearPredictive> findByMachineIdOrderByTimestampDesc(String machineId);

    Flux<ToolWearPredictive> findByMachineIdAndToolOrderByTimestampDesc(String machineId, String tool);
}
