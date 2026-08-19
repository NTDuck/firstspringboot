package com.viettelsoftware.firstspringboot.controller.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class DownloadExportResponse {

    private final @NonNull String url;
}
