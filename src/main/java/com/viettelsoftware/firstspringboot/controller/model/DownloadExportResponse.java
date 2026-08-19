package com.viettelsoftware.firstspringboot.controller.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
public class DownloadExportResponse {
    private final @NonNull String url;
}
