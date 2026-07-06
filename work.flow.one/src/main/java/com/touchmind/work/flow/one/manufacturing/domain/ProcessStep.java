package com.touchmind.work.flow.one.manufacturing.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "processSteps")
public record ProcessStep(

        @Id
        String id,

        @Indexed
        Integer stepNumber,

        String name,

        String machineType,

        String requiredTestType,

        String nextStepId
) {
}
