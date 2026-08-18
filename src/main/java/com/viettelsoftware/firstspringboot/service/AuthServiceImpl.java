package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired(required = false)
    private UserRepository userRepository;

    @Override
    public @Nullable CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return null;
        }
        Jwt token = ((JwtAuthenticationToken) authentication).getToken();

        String subject = token.getSubject();
        String preferredUsername = token.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
        String username = preferredUsername != null && !preferredUsername.isBlank()
                ? preferredUsername
                : subject;

        if (username == null || username.isBlank()) {
            return null;
        }

        long userId = -1L;
        if (subject != null) {
            try {
                userId = Long.parseLong(subject);
            } catch (NumberFormatException ignored) {
                if (userRepository != null) {
                    userId = userRepository.findByKeycloakId(subject)
                            .map(User::getId)
                            .orElse(-1L);
                }
            }
        }

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
