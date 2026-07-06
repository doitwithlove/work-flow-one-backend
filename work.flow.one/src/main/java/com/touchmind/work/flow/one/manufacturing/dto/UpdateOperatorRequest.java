package com.touchmind.work.flow.one.manufacturing.dto;

public record UpdateOperatorRequest(

        String firstName,

        String lastName,

        String role,

        String skillLevel,

        Boolean active
) {
}
