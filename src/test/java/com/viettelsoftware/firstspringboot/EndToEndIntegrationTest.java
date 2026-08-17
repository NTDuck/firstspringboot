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

import static org.hamcrest.Matchers.startsWith;
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
                        .with(jwt().jwt(jwt -> jwt.claim("preferred_username", "testuser"))
                                .authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("testuser"));
    }

    @Test
    void testGetTasksAndExport() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .with(jwt().authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/tasks/export")
                        .with(jwt().authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", startsWith("attachment; filename=\"tasks-")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void testCreateTaskValidation() throws Exception {
        CreateTaskRequest invalidReq = CreateTaskRequest.builder()
                .description(" Invalid Description ")
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .with(jwt().authorities(new SimpleGrantedAuthority("REALM_ROLE_POST")))
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
                        .with(jwt().authorities(new SimpleGrantedAuthority("REALM_ROLE_POST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keycloakId").value("kc-test-123"));

        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/export")
                        .with(jwt().authorities(new SimpleGrantedAuthority("REALM_ROLE_GET"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", startsWith("attachment; filename=\"users-")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }
}
