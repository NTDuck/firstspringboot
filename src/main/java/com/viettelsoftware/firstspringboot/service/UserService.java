package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<@NonNull User> getUsers();

    Optional<@NonNull User> getUserByKeycloakUserId(@NonNull String keycloakUserId);

    @NonNull User createUser(@NonNull User user);
}
