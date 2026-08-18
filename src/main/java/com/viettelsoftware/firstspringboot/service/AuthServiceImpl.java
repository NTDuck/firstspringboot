package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import org.springframework.lang.Nullable;
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
    public @Nullable CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return null;
        }
        Jwt token = ((JwtAuthenticationToken) authentication).getToken();

        long userId = 0L;
        try {
            userId = Long.parseLong(token.getSubject());
        } catch (NumberFormatException ignored) {
        }

        String preferredUsername = token.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
        String username = preferredUsername != null ? preferredUsername : (token.getSubject() != null ? token.getSubject() : "");
        List<String> roles = authentication.getAuthorities()
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
