package com.touchmind.work.flow.one.manufacturing.dto;

import jakarta.validation.constraints.NotBlank;

public record EndOperatorSessionRequest(

        @NotBlank
        String sessionId
) {
}
