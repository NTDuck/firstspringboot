package com.viettelsoftware.firstspringboot.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class AuditEventsQueryFilterDto {

    private final @Nullable LocalDate day;
    private final @Nullable String serviceName;
    private final @Nullable Long actorUserId;
    private final @Nullable String actorUsername;
    private final @Nullable String action;
    private final @Nullable Boolean result;
}
