package com.viettelsoftware.firstspringboot.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedUserDto {
    private final Long id;
    private final String name;
    private final List<String> roles;
}
