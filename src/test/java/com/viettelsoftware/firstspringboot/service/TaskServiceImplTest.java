package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import com.viettelsoftware.firstspringboot.service.dto.TaskWithoutIdDto;
import com.viettelsoftware.firstspringboot.service.exception.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void testCount() {
        when(taskRepository.count()).thenReturn(5L);

        long count = taskService.count();

        assertEquals(5L, count);
        verify(taskRepository, times(1)).count();
    }

    @Test
    void testGetTasks() {
        Task task = Task.builder().description("Test Task").build();
        ReflectionTestUtils.setField(task, "id", 1L);
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> result = taskService.getTasks();

        assertEquals(1, result.size());
        assertEquals("Test Task", result.get(0).getDescription());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testGetTaskByIdSuccess() {
        Task task = Task.builder().description("Test Task").build();
        ReflectionTestUtils.setField(task, "id", 1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals("Test Task", result.getDescription());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(999L));
    }

    @Test
    void testCreateTask() {
        TaskWithoutIdDto dto = TaskWithoutIdDto.builder().description("New Task").build();
        Task savedTask = Task.builder().description("New Task").build();
        ReflectionTestUtils.setField(savedTask, "id", 2L);

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task created = taskService.createTask(dto);

        assertNotNull(created);
        assertEquals("New Task", created.getDescription());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testUpdateTaskSuccess() {
        Task existing = Task.builder().description("Old Task").build();
        ReflectionTestUtils.setField(existing, "id", 1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        TaskWithoutIdDto dto = TaskWithoutIdDto.builder().description("Updated Task").build();
        taskService.updateTask(1L, dto);

        assertEquals("Updated Task", existing.getDescription());
        verify(taskRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateTaskNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskWithoutIdDto dto = TaskWithoutIdDto.builder().description("Updated Task").build();
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(999L, dto));
    }

    @Test
    void testDeleteTaskByIdSuccess() {
        Task existing = Task.builder().description("Task to delete").build();
        ReflectionTestUtils.setField(existing, "id", 1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        taskService.deleteTaskById(1L);

        verify(taskRepository, times(1)).delete(existing);
    }

    @Test
    void testDeleteTaskByIdNotFound() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTaskById(999L));
    }

    @Test
    void testDeleteTasks() {
        taskService.deleteTasks();

        verify(taskRepository, times(1)).deleteAllInBatch();
    }
}
