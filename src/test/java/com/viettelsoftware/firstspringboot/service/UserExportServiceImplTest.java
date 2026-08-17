package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserExportServiceImplTest {

    private final UserExportService userExportService = new UserExportServiceImpl();

    @Test
    void testExportUsers() throws Exception {
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
        assertTrue(result.length > 0, "Exported byte array must not be empty");

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet, "Sheet 0 must exist");
            assertTrue(sheet.getLastRowNum() >= 1, "Sheet must contain header and data rows");
        }
    }
}
