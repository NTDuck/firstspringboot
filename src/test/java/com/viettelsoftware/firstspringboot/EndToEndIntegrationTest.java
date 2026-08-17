package com.viettelsoftware.firstspringboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettelsoftware.firstspringboot.dto.CreateTaskRequest;
import com.viettelsoftware.firstspringboot.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

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

        mockMvc.perform(get("/api/v1/tasks/export")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());
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

        mockMvc.perform(get("/api/v1/users/export")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "1").claim("realm_access", Map.of("roles", List.of("REALM_ROLE_GET"))))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());
    }
}
