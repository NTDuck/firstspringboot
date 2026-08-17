package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserExportServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthService authService;

    @Mock
    private MinIOStorageService minIOStorageService;

    @InjectMocks
    private UserExportServiceImpl userExportService;

    @BeforeEach
    void setUp() {
        lenient().when(authService.getCurrentUser()).thenReturn(CurrentUser.builder().id(1L).name("testuser").roles(List.of()).build());
    }

    @Test
    void testExportUsersContent() {
        User u1 = User.builder()
                .id(1L)
                .keycloakId("k1")
                .name("John Doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userService.getUsers()).thenReturn(List.of(u1));
        when(minIOStorageService.uploadFileAndGetPresignedUrl(any(), any(), any()))
                .thenReturn("http://localhost:9000/firstspringboot/users-export.xlsx");

        String result = userExportService.exportUsers();

        assertNotNull(result);
        assertTrue(result.startsWith("http"));
        verify(minIOStorageService, times(1)).uploadFileAndGetPresignedUrl(any(), any(), any());
    }
}
