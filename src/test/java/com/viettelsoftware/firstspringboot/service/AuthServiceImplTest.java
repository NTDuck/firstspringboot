package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceImplTest {

    private final AuthService authService = new AuthServiceImpl();

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserWithJwtToken() {
        Jwt jwt = new Jwt(
                "token-val",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", "123", "preferred_username", "testuser")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("REALM_ROLE_GET"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        CurrentUser currentUser = authService.getCurrentUser();

        assertNotNull(currentUser);
        assertEquals(123L, currentUser.getId());
        assertEquals("testuser", currentUser.getName());
        assertEquals(List.of("REALM_ROLE_GET"), currentUser.getRoles());
    }
}
