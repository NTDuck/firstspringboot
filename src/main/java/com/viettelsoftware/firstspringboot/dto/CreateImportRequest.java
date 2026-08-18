package com.viettelsoftware.firstspringboot.dto;

import com.viettelsoftware.firstspringboot.entity.Import;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
@Builder
public class CreateImportRequest {
    private final @NonNull Import.Type type;
}
