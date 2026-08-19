package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentAuthenticatedUserWithNumericSubject() {
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

        Optional<AuthenticatedUserDto> result = authenticationService.getCurrentAuthenticatedUser();

        assertTrue(result.isPresent());
        assertEquals(123L, result.get().getId());
        assertEquals("testuser", result.get().getName());
        assertEquals(List.of("REALM_ROLE_GET"), result.get().getRoles());
    }

    @Test
    void testGetCurrentAuthenticatedUserWithKeycloakSubject() {
        Jwt jwt = new Jwt(
                "token-val",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                Map.of("sub", "kc-uuid-123", "preferred_username", "kcuser")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("REALM_ROLE_GET"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = User.builder()
                .keycloakId("kc-uuid-123")
                .name("kcuser")
                .build();
        ReflectionTestUtils.setField(user, "id", 456L);

        when(userRepository.findByKeycloakId("kc-uuid-123")).thenReturn(Optional.of(user));

        Optional<AuthenticatedUserDto> result = authenticationService.getCurrentAuthenticatedUser();

        assertTrue(result.isPresent());
        assertEquals(456L, result.get().getId());
        assertEquals("kcuser", result.get().getName());
    }

    @Test
    void testGetCurrentAuthenticatedUserUnauthenticated() {
        Optional<AuthenticatedUserDto> result = authenticationService.getCurrentAuthenticatedUser();
        assertTrue(result.isEmpty());
    }
}
