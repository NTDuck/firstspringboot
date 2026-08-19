package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.controller.exception.ExportNotFoundException;
import com.viettelsoftware.firstspringboot.service.ExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
                .id(1L)
                .type(Export.Type.TASK)
                .status(Export.Status.SUCCESS)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(10L).build())
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .timeElapsed(100L)
                .url("http://localhost:9000/bucket/tasks-1.xlsx")
                .build();

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
        Export export = Export.builder()
                .id(1L)
                .type(Export.Type.TASK)
                .status(Export.Status.SUCCESS)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(10L).build())
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .timeElapsed(100L)
                .url("http://localhost:9000/bucket/tasks-1.xlsx?sig=xyz")
                .build();

        when(exportService.getById(1L)).thenReturn(export);

        mockMvc.perform(get("/api/v1/exports/1/download")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://localhost:9000/bucket/tasks-1.xlsx?sig=xyz"));
    }

    @Test
    void testDownloadExportNotReadyWhenPending() throws Exception {
        Export export = Export.builder()
                .id(2L)
                .type(Export.Type.USER)
                .status(Export.Status.PENDING)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(10L).build())
                .createdAt(Instant.now())
                .build();

        when(exportService.getById(2L)).thenReturn(export);

        mockMvc.perform(get("/api/v1/exports/2/download")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Export `2` is not ready for download"));
    }

    @Test
    void testDownloadExportNotReadyWhenProcessing() throws Exception {
        Export export = Export.builder()
                .id(3L)
                .type(Export.Type.USER)
                .status(Export.Status.PROCESSING)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(10L).build())
                .createdAt(Instant.now())
                .build();

        when(exportService.getById(3L)).thenReturn(export);

        mockMvc.perform(get("/api/v1/exports/3/download")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Export `3` is not ready for download"));
    }

    @Test
    void testDownloadExportAlreadyFailed() throws Exception {
        Export export = Export.builder()
                .id(4L)
                .type(Export.Type.TASK)
                .status(Export.Status.FAILED)
                .requestedBy(Export.RequestedBy.builder().username("testuser").userId(10L).build())
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .timeElapsed(50L)
                .build();

        when(exportService.getById(4L)).thenReturn(export);

        mockMvc.perform(get("/api/v1/exports/4/download")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Export `4` has already failed"));
    }
}
