package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public @NonNull CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt token = jwtAuth.getToken();

            String id = token.getSubject() != null ? token.getSubject() : "";
            String preferredUsername = token.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
            String name = preferredUsername != null ? preferredUsername : (token.getSubject() != null ? token.getSubject() : "");

            List<String> roles = jwtAuth.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return CurrentUser.builder()
                    .id(id)
                    .name(name)
                    .roles(roles)
                    .build();
        }

        String principalName = authentication != null && authentication.getName() != null ? authentication.getName() : "";
        return CurrentUser.builder()
                .id(principalName)
                .name(principalName)
                .roles(List.of())
                .build();
    }
}
