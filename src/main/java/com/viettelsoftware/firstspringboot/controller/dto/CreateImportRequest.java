package com.viettelsoftware.firstspringboot.controller.dto;

import com.viettelsoftware.firstspringboot.entity.Import;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class CreateImportRequest {

    private final @NonNull Import.Type type;
}
