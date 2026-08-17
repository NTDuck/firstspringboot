package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.Task;
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
class TaskExportServiceImplTest {

    @Mock
    private TaskService taskService;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthService authService;

    @Mock
    private MinIOStorageService minIOStorageService;

    @InjectMocks
    private TaskExportServiceImpl taskExportService;

    @BeforeEach
    void setUp() {
        lenient().when(authService.getCurrentUser()).thenReturn(CurrentUser.builder().id(1L).name("testuser").roles(List.of()).build());
    }

    @Test
    void testExportTasksContent() {
        Task t1 = Task.builder().id(100L).description("First Task").build();
        Task t2 = Task.builder().id(200L).description("Second Task").build();

        when(taskService.getTasks()).thenReturn(List.of(t1, t2));
        when(minIOStorageService.uploadFileAndGetPresignedUrl(any(), any(), any()))
                .thenReturn("http://localhost:9000/firstspringboot/tasks-export.xlsx");

        String result = taskExportService.exportTasks();

        assertNotNull(result);
        assertTrue(result.startsWith("http"));
        verify(minIOStorageService, times(1)).uploadFileAndGetPresignedUrl(any(), any(), any());
    }
}
