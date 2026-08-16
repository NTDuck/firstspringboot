package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.dto.GetUserResponse;
import lombok.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public @NonNull GetUserResponse profile(@NonNull JwtAuthenticationToken auth) {
        return GetUserResponse.builder()
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
}
