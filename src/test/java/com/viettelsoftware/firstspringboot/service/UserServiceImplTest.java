package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.service.dto.AuthenticatedUserDto;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        lenient().when(authenticationService.getCurrentAuthenticatedUser()).thenReturn(AuthenticatedUserDto.builder().id(1L).name("testuser").roles(List.of()).build());
    }

    @Test
    void testGetUsers() {
        User u = User.builder()
                .id(1L)
                .keycloakId("k1")
                .name("John")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(u));

        List<User> result = userService.getUsers();

        assertEquals(1, result.size());
        assertEquals("k1", result.get(0).getKeycloakId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserByKeycloakUserId() {
        User u = User.builder()
                .id(1L)
                .keycloakId("k1")
                .name("John")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findByKeycloakId("k1")).thenReturn(Optional.of(u));

        Optional<User> result = userService.getUserByKeycloakUserId("k1");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getName());
        verify(userRepository, times(1)).findByKeycloakId("k1");
    }

    @Test
    void testCreateUserNew() {
        User u = User.builder()
                .keycloakId("k2")
                .name("Alice")
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Smith")
                .build();

        when(userRepository.findByKeycloakId("k2")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(u);

        User created = userService.createUser(u);

        assertNotNull(created);
        assertEquals("k2", created.getKeycloakId());
        verify(userRepository, times(1)).save(u);
    }
}
