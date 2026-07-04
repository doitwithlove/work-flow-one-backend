package com.touchmind.work.flow.one.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "roles")
public class Role {

    @Id
    private String id;

    private String name;
}
