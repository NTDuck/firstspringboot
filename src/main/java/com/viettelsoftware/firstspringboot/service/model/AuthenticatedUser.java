package com.viettelsoftware.firstspringboot.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedUser {

    private final Long id;
    private final String name;
    private final List<String> roles;
}
