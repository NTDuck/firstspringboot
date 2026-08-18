package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
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
class TaskExportGeneratorImplTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskExportGeneratorImpl taskExportGenerator;

    @Test
    void testGenerateTasksContent() {
        Task t1 = Task.builder().id(100L).description("First Task").build();
        Task t2 = Task.builder().id(200L).description("Second Task").build();

        when(taskService.getTasks()).thenReturn(List.of(t1, t2));

        byte[] result = taskExportGenerator.generate();

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
