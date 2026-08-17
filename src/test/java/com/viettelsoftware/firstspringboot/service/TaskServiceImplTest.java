package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
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
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        lenient().when(authService.getCurrentUser()).thenReturn(CurrentUser.builder().id(1L).name("testuser").roles(List.of()).build());
    }

    @Test
    void testGetTasks() {
        Task task = Task.builder().id(1L).description("Test Task").build();
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> result = taskService.getTasks();

        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getDescription());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testGetTaskById() {
        Task task = Task.builder().id(1L).description("Test Task").build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.getTaskById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Task", result.get().getDescription());
        verify(taskRepository, times(1)).findById(1L);
    }
}
