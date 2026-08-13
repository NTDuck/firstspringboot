package com.viettelsoftware.firstspringboot.auth.controller;

import com.viettelsoftware.firstspringboot.auth.dto.GetUserResponse;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    @GetMapping("/profile")
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
