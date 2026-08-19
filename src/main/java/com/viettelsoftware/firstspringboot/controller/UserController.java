package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.controller.dto.CreateExportRequest;
import com.viettelsoftware.firstspringboot.controller.dto.CreateImportRequest;
import com.viettelsoftware.firstspringboot.controller.dto.CreateUserRequest;
import com.viettelsoftware.firstspringboot.controller.dto.GetUserResponse;
import com.viettelsoftware.firstspringboot.entity.Export;
import com.viettelsoftware.firstspringboot.entity.Import;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.config.exception.InsufficientAuthorizationException;
import com.viettelsoftware.firstspringboot.service.AuthenticationService;
import com.viettelsoftware.firstspringboot.service.ExportService;
import com.viettelsoftware.firstspringboot.service.ImportService;
import com.viettelsoftware.firstspringboot.service.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private ImportService importService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public @NonNull GetUserResponse profile() {
        var currentUser = authenticationService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new InsufficientAuthorizationException("anonymous", "get user profile");
        }
        return GetUserResponse.builder()
                .name(currentUser.getName())
                .roles(currentUser.getRoles())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    public List<@NonNull User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('REALM_ROLE_GET')")
    public ResponseEntity<Void> exportUsers() {
        Export export = exportService.create(CreateExportRequest.of(Export.Type.USER));
        return ResponseEntity.accepted()
                .location(URI.create(String.format("/api/v1/exports/%d", export.getId())))
                .build();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('REALM_ROLE_USER_CREATE') and hasAuthority('REALM_ROLE_POST')")
    public ResponseEntity<Void> importUsers() {
        Import importEntity = importService.create(CreateImportRequest.of(Import.Type.USER));
        return ResponseEntity.accepted()
                .location(URI.create(String.format("/api/v1/imports/%d", importEntity.getId())))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('REALM_ROLE_USER_CREATE') and hasAuthority('REALM_ROLE_POST')")
    public @NonNull User createUser(@Valid @RequestBody @NonNull CreateUserRequest request) {
        User user = User.builder()
                .keycloakId(request.getKeycloakId())
                .name(request.getName())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        return userService.createUser(user);
    }
}
