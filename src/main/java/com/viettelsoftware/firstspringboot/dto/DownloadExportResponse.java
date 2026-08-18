package com.viettelsoftware.firstspringboot.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
public class DownloadExportResponse {
    private final @NonNull String url;
}
