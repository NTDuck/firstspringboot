package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.config.exception.InsufficientAuthorizationException;
import com.viettelsoftware.firstspringboot.controller.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.controller.dto.CreateImportRequest;
import com.viettelsoftware.firstspringboot.controller.dto.CreateUserRequest;
import com.viettelsoftware.firstspringboot.controller.dto.GetUserResponse;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.service.AuthenticationService;
import com.viettelsoftware.firstspringboot.service.ExportService;
import com.viettelsoftware.firstspringboot.service.ImportService;
import com.viettelsoftware.firstspringboot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final ExportService exportService;
    private final ImportService importService;
    private final AuthenticationService authenticationService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public GetUserResponse profile() {
        val currentUser = authenticationService.getCurrentAuthenticatedUser()
                .orElseThrow(() -> InsufficientAuthorizationException.builder()
                        .username("anonymous")
                        .operation("get user profile")
                        .build());

        return GetUserResponse.builder()
                .name(currentUser.getName())
                .roles(currentUser.getRoles())
                .build();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    @GetMapping("/export")
    public ResponseEntity<Void> exportUsers() {
        val export = exportService.create(CreateExportRequest.of(Export.Type.USER));
        return ResponseEntity.accepted()
                .location(URI.create(String.format("/api/v1/exports/%d", export.getId())))
                .build();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_USER_CREATE') and hasAuthority('REALM_ROLE_POST')")
    @PostMapping("/import")
    public ResponseEntity<Void> importUsers() {
        val import_ = importService.create(CreateImportRequest.of(Import.Type.USER));
        return ResponseEntity.accepted()
                .location(URI.create(String.format("/api/v1/imports/%d", import_.getId())))
                .build();
    }

    @PreAuthorize("hasAuthority('REALM_ROLE_USER_CREATE') and hasAuthority('REALM_ROLE_POST')")
    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
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
