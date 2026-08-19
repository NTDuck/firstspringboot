package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.annotation.Auditable;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Value("${keycloak.admin.server-url:}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm:master}")
    private String adminRealm;

    @Value("${keycloak.admin.target-realm:firstspringbootrealm}")
    private String targetRealm;

    @Value("${keycloak.admin.username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin.password:admin}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Override
    @Auditable
    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    @Auditable
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = "#keycloakUserId")
    public Optional<User> getUserByKeycloakUserId(String keycloakUserId) {
        return userRepository.findByKeycloakId(keycloakUserId);
    }

    @Override
    @Auditable
    @Transactional
    public User createUser(User user) {
        val userWithKeycloakId = syncUserWithKeycloak(user);
        return saveOrUpdateUser(userWithKeycloakId);
    }

    private User syncUserWithKeycloak(User user) {
        val createdKeycloakId = createKeycloakUser(user);
        if (createdKeycloakId.isEmpty()) return user;

        user.setKeycloakId(createdKeycloakId.get());
        return user;
    }

    private Optional<String> createKeycloakUser(User user) {
        if (keycloakServerUrl == null || keycloakServerUrl.isBlank()) return Optional.empty();

        try {
            val keycloak = buildKeycloakClient();
            val userRep = buildUserRepresentation(user);

            try (val response = keycloak.realm(targetRealm).users().create(userRep)) {
                return extractKeycloakIdFromResponse(response);
            }
        } catch (Exception ignored) {
            // User may already exist in Keycloak or Keycloak is unavailable, proceed to local DB save
            return Optional.empty();
        }
    }

    private Keycloak buildKeycloakClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm(adminRealm)
                .username(adminUsername)
                .password(adminPassword)
                .clientId(adminClientId)
                .build();
    }

    private UserRepresentation buildUserRepresentation(User user) {
        val userRep = new UserRepresentation();
        userRep.setUsername(user.getName());
        userRep.setEmail(user.getEmail());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEnabled(true);
        return userRep;
    }

    private Optional<String> extractKeycloakIdFromResponse(Response response) {
        if (response.getStatus() != 201 || response.getLocation() == null) return Optional.empty();

        val path = response.getLocation().getPath();
        val keycloakId = path.substring(path.lastIndexOf('/') + 1);
        if (keycloakId.isBlank()) return Optional.empty();

        return Optional.of(keycloakId);
    }

    private User saveOrUpdateUser(User user) {
        return userRepository.findByKeycloakId(user.getKeycloakId())
                .map(existing -> updateUserFields(existing, user))
                .orElseGet(() -> userRepository.save(user));
    }

    private User updateUserFields(User existing, User user) {
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        return userRepository.save(existing);
    }
}
