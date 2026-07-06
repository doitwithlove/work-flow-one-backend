package com.touchmind.work.flow.one.manufacturing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;

@Document(collection = "shifts")
public record Shift(

        @Id
        String id,

        @Indexed(unique = true)
        String name,

        LocalTime startTime,

        LocalTime endTime,

        Integer targetOutput,

        Boolean active
) {
}
