package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.Task;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.ImportRepository;
import com.viettelsoftware.firstspringboot.repository.TaskRepository;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceImplProcessorTest {

    @Mock
    private ImportRepository importRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectStorageService objectStorageService;

    @InjectMocks
    private ImportServiceImpl.Processor processor;

    private byte[] createTasksExcel(String[][] data) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Tasks");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Description");

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                if (data[i][0] != null) row.createCell(0).setCellValue(data[i][0]);
                if (data[i][1] != null) row.createCell(1).setCellValue(data[i][1]);
            }
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private byte[] createUsersExcel(String[][] data) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Keycloak ID");
            header.createCell(2).setCellValue("Name");
            header.createCell(3).setCellValue("Email");
            header.createCell(4).setCellValue("First Name");
            header.createCell(5).setCellValue("Last Name");

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int c = 0; c < data[i].length; c++) {
                    if (data[i][c] != null) row.createCell(c).setCellValue(data[i][c]);
                }
            }
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    @Test
    void testProcessValidTaskImport() throws Exception {
        Import importEntity = Import.builder()
                .type(Import.Type.TASK)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 1L);

        byte[] excelBytes = createTasksExcel(new String[][]{
                {"1", "Clean Desk"},
                {null, "Buy Groceries"}
        });

        when(importRepository.findById(1L)).thenReturn(Optional.of(importEntity));
        when(objectStorageService.exists("imports-1.xlsx")).thenReturn(true);
        when(objectStorageService.get("imports-1.xlsx"))
                .thenReturn(new ByteArrayInputStream(excelBytes))
                .thenReturn(new ByteArrayInputStream(excelBytes));

        Task existingTask = Task.builder().description("Old").build();
        ReflectionTestUtils.setField(existingTask, "id", 1L);

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        processor.process(1L);

        verify(taskRepository, atLeastOnce()).save(any(Task.class));
        ArgumentCaptor<Import> captor = ArgumentCaptor.forClass(Import.class);
        verify(importRepository, atLeast(2)).save(captor.capture());

        Import finalSaved = captor.getValue();
        assertEquals(Import.Status.SUCCESS, finalSaved.getStatus());
        assertNotNull(finalSaved.getCompletedAt());
    }

    @Test
    void testProcessInvalidTaskRowFails() throws Exception {
        Import importEntity = Import.builder()
                .type(Import.Type.TASK)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 2L);

        // Leading/trailing whitespace violates task description rule
        byte[] excelBytes = createTasksExcel(new String[][]{
                {"1", " Invalid Description "}
        });

        when(importRepository.findById(2L)).thenReturn(Optional.of(importEntity));
        when(objectStorageService.exists("imports-2.xlsx")).thenReturn(true);
        when(objectStorageService.get("imports-2.xlsx")).thenReturn(new ByteArrayInputStream(excelBytes));

        processor.process(2L);

        verify(taskRepository, never()).save(any(Task.class));
        ArgumentCaptor<Import> captor = ArgumentCaptor.forClass(Import.class);
        verify(importRepository, atLeast(2)).save(captor.capture());

        Import finalSaved = captor.getValue();
        assertEquals(Import.Status.FAILED, finalSaved.getStatus());
    }

    @Test
    void testProcessTaskCrossRowDuplicateIdFails() throws Exception {
        Import importEntity = Import.builder()
                .type(Import.Type.TASK)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 3L);

        // Duplicate ID 10
        byte[] excelBytes = createTasksExcel(new String[][]{
                {"10", "Task A"},
                {"10", "Task B"}
        });

        when(importRepository.findById(3L)).thenReturn(Optional.of(importEntity));
        when(objectStorageService.exists("imports-3.xlsx")).thenReturn(true);
        when(objectStorageService.get("imports-3.xlsx")).thenReturn(new ByteArrayInputStream(excelBytes));

        processor.process(3L);

        verify(taskRepository, never()).save(any(Task.class));
        ArgumentCaptor<Import> captor = ArgumentCaptor.forClass(Import.class);
        verify(importRepository, atLeast(2)).save(captor.capture());

        Import finalSaved = captor.getValue();
        assertEquals(Import.Status.FAILED, finalSaved.getStatus());
    }

    @Test
    void testProcessValidUserImport() throws Exception {
        Import importEntity = Import.builder()
                .type(Import.Type.USER)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 4L);

        byte[] excelBytes = createUsersExcel(new String[][]{
                {"1", "kc-123", "alice", "alice@example.com", "Alice", "Smith"}
        });

        when(importRepository.findById(4L)).thenReturn(Optional.of(importEntity));
        when(objectStorageService.exists("imports-4.xlsx")).thenReturn(true);
        when(objectStorageService.get("imports-4.xlsx"))
                .thenReturn(new ByteArrayInputStream(excelBytes))
                .thenReturn(new ByteArrayInputStream(excelBytes));

        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.empty());

        processor.process(4L);

        verify(userRepository, atLeastOnce()).save(any(User.class));
        ArgumentCaptor<Import> captor = ArgumentCaptor.forClass(Import.class);
        verify(importRepository, atLeast(2)).save(captor.capture());

        Import finalSaved = captor.getValue();
        assertEquals(Import.Status.SUCCESS, finalSaved.getStatus());
    }

    @Test
    void testProcessInvalidUserRowFails() throws Exception {
        Import importEntity = Import.builder()
                .type(Import.Type.USER)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 5L);

        // Invalid email format
        byte[] excelBytes = createUsersExcel(new String[][]{
                {"1", "kc-123", "alice", "not-an-email", "Alice", "Smith"}
        });

        when(importRepository.findById(5L)).thenReturn(Optional.of(importEntity));
        when(objectStorageService.exists("imports-5.xlsx")).thenReturn(true);
        when(objectStorageService.get("imports-5.xlsx")).thenReturn(new ByteArrayInputStream(excelBytes));

        processor.process(5L);

        verify(userRepository, never()).save(any(User.class));
        ArgumentCaptor<Import> captor = ArgumentCaptor.forClass(Import.class);
        verify(importRepository, atLeast(2)).save(captor.capture());

        Import finalSaved = captor.getValue();
        assertEquals(Import.Status.FAILED, finalSaved.getStatus());
    }

    @Test
    void testProcessUserCrossRowDuplicateUsernameFails() throws Exception {
        Import importEntity = Import.builder()
                .type(Import.Type.USER)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 6L);

        // Duplicate username "alice"
        byte[] excelBytes = createUsersExcel(new String[][]{
                {"1", "kc-1", "alice", "alice1@example.com", "Alice", "One"},
                {"2", "kc-2", "alice", "alice2@example.com", "Alice", "Two"}
        });

        when(importRepository.findById(6L)).thenReturn(Optional.of(importEntity));
        when(objectStorageService.exists("imports-6.xlsx")).thenReturn(true);
        when(objectStorageService.get("imports-6.xlsx")).thenReturn(new ByteArrayInputStream(excelBytes));

        processor.process(6L);

        verify(userRepository, never()).save(any(User.class));
        ArgumentCaptor<Import> captor = ArgumentCaptor.forClass(Import.class);
        verify(importRepository, atLeast(2)).save(captor.capture());

        Import finalSaved = captor.getValue();
        assertEquals(Import.Status.FAILED, finalSaved.getStatus());
    }

    @Test
    void testProcessMalformedFileFails() {
        Import importEntity = Import.builder()
                .type(Import.Type.TASK)
                .status(Import.Status.PENDING)
                .build();
        ReflectionTestUtils.setField(importEntity, "id", 7L);

        when(importRepository.findById(7L)).thenReturn(Optional.of(importEntity));
        when(objectStorageService.exists("imports-7.xlsx")).thenReturn(true);
        when(objectStorageService.get("imports-7.xlsx")).thenReturn(new ByteArrayInputStream(new byte[]{0, 1, 2, 3}));

        processor.process(7L);

        ArgumentCaptor<Import> captor = ArgumentCaptor.forClass(Import.class);
        verify(importRepository, atLeast(2)).save(captor.capture());

        Import finalSaved = captor.getValue();
        assertEquals(Import.Status.FAILED, finalSaved.getStatus());
    }
}
