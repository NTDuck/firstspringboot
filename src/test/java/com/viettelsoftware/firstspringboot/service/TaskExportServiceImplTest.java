package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.Task;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskExportServiceImplTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskExportServiceImpl taskExportService;

    @Test
    void testExportTasksContent() throws Exception {
        Task t1 = Task.builder().id(100L).description("First Task").build();
        Task t2 = Task.builder().id(200L).description("Second Task").build();

        when(taskService.getTasks()).thenReturn(List.of(t1, t2));

        byte[] result = taskExportService.exportTasks();

        assertNotNull(result);
        assertTrue(result.length > 0, "Byte array length must be > 0");

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet, "Sheet 0 must exist");

            // Row 0: Headers
            Row headerRow = sheet.getRow(0);
            assertEquals("ID", headerRow.getCell(0).getStringCellValue());
            assertEquals("Description", headerRow.getCell(1).getStringCellValue());

            // Row 1: Task 1
            Row r1 = sheet.getRow(1);
            assertNotNull(r1, "Row 1 must exist");
            assertEquals("First Task", r1.getCell(1).getStringCellValue());

            // Row 2: Task 2
            Row r2 = sheet.getRow(2);
            assertNotNull(r2, "Row 2 must exist");
            assertEquals("Second Task", r2.getCell(1).getStringCellValue());
        }
    }
}
