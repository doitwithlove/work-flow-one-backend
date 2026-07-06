package com.touchmind.work.flow.one.manufacturing.repository;

import com.touchmind.work.flow.one.manufacturing.domain.OperatorMachineSession;
import com.touchmind.work.flow.one.manufacturing.enums.OperatorSessionStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperatorMachineSessionRepository extends ReactiveMongoRepository<OperatorMachineSession, String> {

    Mono<OperatorMachineSession> findFirstByOperatorIdAndStatus(String operatorId, OperatorSessionStatus status);

    Mono<OperatorMachineSession> findFirstByMachineIdAndStatus(String machineId, OperatorSessionStatus status);

    Flux<OperatorMachineSession> findByOperatorIdOrderByLoginTimeDesc(String operatorId);

    Flux<OperatorMachineSession> findByMachineIdOrderByLoginTimeDesc(String machineId);

    Flux<OperatorMachineSession> findByStatusOrderByLoginTimeDesc(OperatorSessionStatus status);
}
