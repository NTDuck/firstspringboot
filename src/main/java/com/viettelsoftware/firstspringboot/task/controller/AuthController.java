package com.viettelsoftware.firstspringboot.task.controller;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    @GetMapping("/profile")
    public @NonNull UserDto profile(@NonNull JwtAuthenticationToken auth) {
        return UserDto.builder()
                .name(auth
                        .getToken()
                        .getClaimAsString(StandardClaimNames.PREFERRED_USERNAME))
                .roles(auth
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Setter
    @Builder
    public static class UserDto {
        @NonNull String name;
        List<@NonNull String> roles;
    }
}
