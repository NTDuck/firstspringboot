package com.viettelsoftware.firstspringboot.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetUserResponse {

    private final @NonNull String name;
    private final List<@NonNull String> roles;
}
