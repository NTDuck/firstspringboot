package com.viettelsoftware.firstspringboot.controller.dto;

import com.viettelsoftware.firstspringboot.entity.Export;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class CreateExportRequest {

    private final @NonNull Export.Type type;
}
