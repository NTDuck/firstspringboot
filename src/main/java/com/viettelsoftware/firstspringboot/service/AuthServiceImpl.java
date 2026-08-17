package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public @Nullable CurrentUser getCurrentUser() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication instanceof JwtAuthenticationToken;

        val token = ((JwtAuthenticationToken) authentication).getToken();

        val userId = Long.parseLong(token.getSubject());
        val username = token.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
        val roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return CurrentUser.builder()
                .id(userId)
                .name(username)
                .roles(roles)
                .build();
    }
}
