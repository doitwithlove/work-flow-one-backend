package com.touchmind.work.flow.one.manufacturing.dto;

import java.time.LocalTime;

public record ShiftResponse(

        String id,

        String name,

        LocalTime startTime,

        LocalTime endTime,

        Integer targetOutput,

        Boolean active
) {
}
