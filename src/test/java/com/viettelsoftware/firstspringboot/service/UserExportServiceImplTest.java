package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
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
class UserExportServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserExportServiceImpl userExportService;

    @Test
    void testExportUsersContent() throws Exception {
        User u1 = User.builder()
                .id(1L)
                .keycloakId("k1")
                .name("John Doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userService.getUsers()).thenReturn(List.of(u1));

        byte[] result = userExportService.exportUsers();

        assertNotNull(result);
        assertTrue(result.length > 0, "Exported byte array must not be empty");

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet, "Sheet 0 must exist");

            // Row 0: Headers
            Row r0 = sheet.getRow(0);
            assertEquals("ID", r0.getCell(0).getStringCellValue());
            assertEquals("Keycloak ID", r0.getCell(1).getStringCellValue());
            assertEquals("Name", r0.getCell(2).getStringCellValue());
            assertEquals("Email", r0.getCell(3).getStringCellValue());
            assertEquals("First Name", r0.getCell(4).getStringCellValue());
            assertEquals("Last Name", r0.getCell(5).getStringCellValue());

            // Row 1: Evaluated User Data
            Row r1 = sheet.getRow(1);
            assertNotNull(r1, "Row 1 must exist");
            assertEquals("k1", r1.getCell(1).getStringCellValue());
            assertEquals("John Doe", r1.getCell(2).getStringCellValue());
            assertEquals("john@example.com", r1.getCell(3).getStringCellValue());
            assertEquals("John", r1.getCell(4).getStringCellValue());
            assertEquals("Doe", r1.getCell(5).getStringCellValue());
        }
    }
}
