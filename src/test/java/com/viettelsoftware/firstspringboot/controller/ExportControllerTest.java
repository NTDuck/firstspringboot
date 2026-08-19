package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.controller.exception.ExportAlreadyFailedException;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotFoundException;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotReadyException;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.service.ExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @Test
    void testGetExportStatusSuccess() throws Exception {
        Export export = Export.builder()
                .type(Export.Type.TASK)
                .status(Export.Status.SUCCESS)
                .url("http://localhost:9000/bucket/tasks-1.xlsx")
                .build();
        ReflectionTestUtils.setField(export, "id", 1L);
        ReflectionTestUtils.setField(export, "createdAt", Instant.now());
        ReflectionTestUtils.setField(export, "completedAt", Instant.now());

        when(exportService.getById(1L)).thenReturn(export);

        mockMvc.perform(get("/api/v1/exports/1")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("TASK"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.url").value("http://localhost:9000/bucket/tasks-1.xlsx"));
    }

    @Test
    void testGetExportStatusNotFound() throws Exception {
        when(exportService.getById(999L)).thenThrow(ExportNotFoundException.of(999L));

        mockMvc.perform(get("/api/v1/exports/999")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Export `999` not found"));
    }

    @Test
    void testDownloadExportSuccess() throws Exception {
        when(exportService.getDownloadUrl(1L)).thenReturn("http://localhost:9000/bucket/tasks-1.xlsx?sig=xyz");

        mockMvc.perform(get("/api/v1/exports/1/download")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://localhost:9000/bucket/tasks-1.xlsx?sig=xyz"));
    }

    @Test
    void testDownloadExportNotReadyWhenPending() throws Exception {
        when(exportService.getDownloadUrl(2L)).thenThrow(ExportNotReadyException.of(2L));

        mockMvc.perform(get("/api/v1/exports/2/download")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Export `2` is not ready for download"));
    }

    @Test
    void testDownloadExportAlreadyFailed() throws Exception {
        when(exportService.getDownloadUrl(4L)).thenThrow(ExportAlreadyFailedException.of(4L));

        mockMvc.perform(get("/api/v1/exports/4/download")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Export `4` has already failed"));
    }
}
