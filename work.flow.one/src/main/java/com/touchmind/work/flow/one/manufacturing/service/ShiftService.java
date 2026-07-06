package com.touchmind.work.flow.one.manufacturing.service;

import com.touchmind.work.flow.one.exception.ApiException;
import com.touchmind.work.flow.one.manufacturing.domain.Shift;
import com.touchmind.work.flow.one.manufacturing.dto.CreateShiftRequest;
import com.touchmind.work.flow.one.manufacturing.dto.ShiftResponse;
import com.touchmind.work.flow.one.manufacturing.repository.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public Mono<ShiftResponse> create(CreateShiftRequest request) {
        Shift shift = new Shift(
                null,
                request.name().trim(),
                request.startTime(),
                request.endTime(),
                request.targetOutput(),
                request.active());
        return shiftRepository.save(shift).map(this::toResponse);
    }

    public Flux<ShiftResponse> list() {
        return shiftRepository.findAll().map(this::toResponse);
    }

    public Mono<ShiftResponse> get(String id) {
        return shiftRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Shift not found")))
                .map(this::toResponse);
    }

    public Mono<Shift> findDomain(String id) {
        return shiftRepository.findById(id)
                .switchIfEmpty(Mono.error(new ApiException(HttpStatus.NOT_FOUND, "Shift not found")));
    }

    private ShiftResponse toResponse(Shift shift) {
        return new ShiftResponse(
                shift.id(),
                shift.name(),
                shift.startTime(),
                shift.endTime(),
                shift.targetOutput(),
                shift.active());
    }
}
