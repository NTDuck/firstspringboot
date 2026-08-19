package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.controller.exception.ImportNotFoundException;
import com.viettelsoftware.firstspringboot.service.ImportService;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImportService importService;

    @Test
    void testGetImportStatusSuccess() throws Exception {
        Import importEntity = Import.builder()
                .id(1L)
                .type(Import.Type.TASK)
                .status(Import.Status.SUCCESS)
                .requestedBy(Import.RequestedBy.builder().username("testuser").userId(10L).build())
                .createdAt(Instant.now())
                .completedAt(Instant.now())
                .timeElapsed(100L)
                .url("http://localhost:9000/bucket/imports-1.xlsx?upload=true")
                .build();

        when(importService.getById(1L)).thenReturn(importEntity);

        mockMvc.perform(get("/api/v1/imports/1")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("TASK"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.url").value("http://localhost:9000/bucket/imports-1.xlsx?upload=true"));
    }

    @Test
    void testGetImportStatusNotFound() throws Exception {
        when(importService.getById(999L)).thenThrow(ImportNotFoundException.of(999L));

        mockMvc.perform(get("/api/v1/imports/999")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Import `999` not found"));
    }

}
