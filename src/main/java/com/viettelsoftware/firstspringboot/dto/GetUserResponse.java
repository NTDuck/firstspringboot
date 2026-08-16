package com.viettelsoftware.firstspringboot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@Builder
public class GetUserResponse {
    private final @NonNull String name;
    private final List<@NonNull String> roles;
}
