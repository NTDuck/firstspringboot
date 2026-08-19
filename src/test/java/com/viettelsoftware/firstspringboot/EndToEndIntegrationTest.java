package com.viettelsoftware.firstspringboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.firstspringboot.controller.dto.CreateTaskRequest;
import com.viettelsoftware.firstspringboot.controller.dto.CreateUserRequest;
import com.viettelsoftware.firstspringboot.service.ObjectStorageService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
class EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObjectStorageService objectStorageService;

    private byte[] createTasksExcel(String[][] data) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
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
        try (Workbook wb = new XSSFWorkbook(); java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
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
    void testUnauthorizedAccessBlocked() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProfileEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("preferred_username", "integrationtestuser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("integrationtestuser"));
    }

    @Test
    void testGetTasksAndExport() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk());

        String location = mockMvc.perform(get("/api/v1/tasks/export")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", matchesPattern("^/api/v1/exports/\\d+$")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assert location != null;
        mockMvc.perform(get(location)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TASK"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testTaskImportEndpoint() throws Exception {
        String location = mockMvc.perform(post("/api/v1/tasks/import")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_POST"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_POST"))))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", matchesPattern("^/api/v1/imports/\\d+$")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assert location != null;
        long importId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        byte[] fileBytes = createTasksExcel(new String[][]{
                {null, "Imported Integration Task A"},
                {null, "Imported Integration Task B"}
        });

        objectStorageService.put("imports-" + importId + ".xlsx", fileBytes, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        mockMvc.perform(get("/api/v1/imports/" + importId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TASK"))
                .andExpect(jsonPath("$.status").exists());
    }
    @Test
    void testCreateTaskValidation() throws Exception {
        CreateTaskRequest invalidReq = CreateTaskRequest.builder()
                .description(" Invalid Description ")
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_POST"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_POST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserAndExport() throws Exception {
        CreateUserRequest req = CreateUserRequest.builder()
                .keycloakId("kc-test-123")
                .name("Integration User")
                .email("test@example.com")
                .firstName("Integration")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_POST", "REALM_ROLE_USER_CREATE"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_POST"), new SimpleGrantedAuthority("REALM_ROLE_USER_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").exists());

        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk());

        String location = mockMvc.perform(get("/api/v1/users/export")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", matchesPattern("^/api/v1/exports/\\d+$")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assert location != null;
        mockMvc.perform(get(location)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("USER"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testUserImportEndpoint() throws Exception {
        String location = mockMvc.perform(post("/api/v1/users/import")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_POST", "REALM_ROLE_USER_CREATE"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_POST"), new SimpleGrantedAuthority("REALM_ROLE_USER_CREATE"))))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", matchesPattern("^/api/v1/imports/\\d+$")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        assert location != null;
        long importId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        byte[] fileBytes = createUsersExcel(new String[][]{
                {null, "kc-e2e-user", "e2euser", "e2euser@example.com", "E2E", "User"}
        });

        objectStorageService.put("imports-" + importId + ".xlsx", fileBytes, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        mockMvc.perform(get("/api/v1/imports/" + importId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("USER"))
                .andExpect(jsonPath("$.status").exists());
    }
}
