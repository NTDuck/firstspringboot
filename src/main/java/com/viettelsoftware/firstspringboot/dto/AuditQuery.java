package com.viettelsoftware.firstspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class AuditQuery {
    private @Nullable LocalDate day;
    private @Nullable String serviceName;
    private @Nullable Long actorUserId;
    private @Nullable String actorUsername;
    private @Nullable String action;
    private @Nullable Boolean result;
}
