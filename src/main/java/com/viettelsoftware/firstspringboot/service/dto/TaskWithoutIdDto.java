package com.viettelsoftware.firstspringboot.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.viettelsoftware.firstspringboot.validation.ValidTaskDescription;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
public class TaskWithoutIdDto {

    @ValidTaskDescription
    @JsonProperty("description")
    private final @NonNull String description;
}
