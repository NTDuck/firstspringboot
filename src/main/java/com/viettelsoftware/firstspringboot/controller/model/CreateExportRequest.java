package com.viettelsoftware.firstspringboot.controller.model;

import com.viettelsoftware.firstspringboot.entity.Export;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@Builder
public class CreateExportRequest {
    private final @NonNull Export.Type type;
}
