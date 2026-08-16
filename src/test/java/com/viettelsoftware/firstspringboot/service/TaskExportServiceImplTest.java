package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskExportServiceImplTest {

    private final TaskExportService taskExportService = new TaskExportServiceImpl();

    @Test
    void testExportTasks() {
        Task t1 = Task.builder().id(1L).description("Task 1").build();
        Task t2 = Task.builder().id(2L).description("Task 2").build();

        byte[] result = taskExportService.exportTasks(List.of(t1, t2));

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
