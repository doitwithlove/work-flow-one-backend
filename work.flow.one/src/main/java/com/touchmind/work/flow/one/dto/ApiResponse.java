package com.touchmind.work.flow.one.dto;

import java.time.Instant;

public record ApiResponse(

        Instant timestamp,

        int status,

        String message,

        Object data

) {
}