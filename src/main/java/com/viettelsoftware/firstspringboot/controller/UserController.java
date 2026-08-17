package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.dto.CreateUserRequest;
import com.viettelsoftware.firstspringboot.dto.GetUserResponse;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.service.UserExportService;
import com.viettelsoftware.firstspringboot.service.UserService;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserExportService userExportService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public @NonNull GetUserResponse profile(@NonNull JwtAuthenticationToken auth) {
        return GetUserResponse.builder()
                .name(auth
                        .getToken()
                        .getClaimAsString(StandardClaimNames.PREFERRED_USERNAME))
                .roles(auth
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    public List<@NonNull User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    public ResponseEntity<byte[]> exportUsers() {
        val users = userService.getUsers();
        val excelBytes = userExportService.exportUsers(users);

        val timestamp = OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_DATE_TIME);
        val filename = String.format("users-%s.xlsx", timestamp);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename))
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REALM_ROLE_POST')")
    public @NonNull User createUser(@Valid @RequestBody @NonNull CreateUserRequest request) {
        val user = User.builder()
                .keycloakId(request.getKeycloakId())
                .name(request.getName())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        return userService.createUser(user);
    }
}
