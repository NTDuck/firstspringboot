package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserExportServiceImplTest {

    private final UserExportService userExportService = new UserExportServiceImpl();

    @Test
    void testExportUsers() {
        User u1 = User.builder()
                .id(1L)
                .keycloakId("k1")
                .name("John Doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        byte[] result = userExportService.exportUsers(List.of(u1));

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
