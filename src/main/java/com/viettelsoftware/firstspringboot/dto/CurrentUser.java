package com.viettelsoftware.firstspringboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Getter
@NoArgsConstructor(force = true, access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class CurrentUser {
    private final @NonNull String id;
    private final @NonNull String name;
    private final List<@NonNull String> roles;
}
