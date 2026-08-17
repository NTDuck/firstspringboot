package com.viettelsoftware.firstspringboot.controller;

import com.viettelsoftware.firstspringboot.dto.CreateUserRequest;
import com.viettelsoftware.firstspringboot.dto.GetUserResponse;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.service.AuthService;
import com.viettelsoftware.firstspringboot.service.UserExportService;
import com.viettelsoftware.firstspringboot.service.UserService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserExportService userExportService;

    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public @NonNull GetUserResponse profile() {
        var currentUser = authService.getCurrentUser();
        assert currentUser != null;

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
    public Map<@NonNull String, @NonNull String> exportUsers() {
        String url = userExportService.exportUsers();
        return Map.of("url", url);
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
