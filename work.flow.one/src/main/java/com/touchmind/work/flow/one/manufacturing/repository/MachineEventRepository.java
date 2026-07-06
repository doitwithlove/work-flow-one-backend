package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.MachineEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MachineEventRepository extends ReactiveMongoRepository<MachineEvent, String> {

    Flux<MachineEvent> findByPartIdOrderByReceivedAtDesc(String partId);

    Flux<MachineEvent> findByMachineIdOrderByReceivedAtDesc(String machineId);

    Flux<MachineEvent> findByOperatorIdOrderByReceivedAtDesc(String operatorId);

    Flux<MachineEvent> findByOperatorSessionIdOrderByReceivedAtDesc(String operatorSessionId);

    Mono<MachineEvent> findFirstByPartIdOrderByReceivedAtDesc(String partId);
}
