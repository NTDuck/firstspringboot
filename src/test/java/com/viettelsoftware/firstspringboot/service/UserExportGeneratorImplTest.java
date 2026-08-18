package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserExportGeneratorImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserExportGeneratorImpl userExportGenerator;

    @Test
    void testGenerateUsersContent() {
        User u1 = User.builder()
                .id(1L)
                .keycloakId("k1")
                .name("John Doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userService.getUsers()).thenReturn(List.of(u1));

        byte[] result = userExportGenerator.generate();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
