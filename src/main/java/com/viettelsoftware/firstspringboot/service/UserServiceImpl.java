package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import lombok.NonNull;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuthService authService;

    @Value("${keycloak.admin.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    @Value("${keycloak.admin.target-realm}")
    private String targetRealm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Override
    public List<@NonNull User> getUsers() {
        audit("GET_USERS");
        return userRepository.findAll();
    }

    @Override
    public Optional<@NonNull User> getUserByKeycloakUserId(@NonNull String keycloakUserId) {
        audit("GET_USER_BY_KEYCLOAK_USER_ID");
        return userRepository.findByKeycloakId(keycloakUserId);
    }

    @Override
    public @NonNull User createUser(@NonNull User user) {
        User userToSave = user;
        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(adminRealm)
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId(adminClientId)
                    .build();

            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(user.getName());
            userRep.setEmail(user.getEmail());
            userRep.setFirstName(user.getFirstName());
            userRep.setLastName(user.getLastName());
            userRep.setEnabled(true);

            try (Response response = keycloak.realm(targetRealm).users().create(userRep)) {
                if (response.getStatus() == 201 && response.getLocation() != null) {
                    String path = response.getLocation().getPath();
                    String keycloakId = path.substring(path.lastIndexOf('/') + 1);
                    if (!keycloakId.isBlank()) {
                        userToSave = user.withKeycloakId(keycloakId);
                    }
                }
            }
        } catch (Exception ignored) {
            // User may already exist in Keycloak or Keycloak unavailable, proceed to local DB save
        }

        final User finalUserToSave = userToSave;
        User saved = userRepository.findByKeycloakId(finalUserToSave.getKeycloakId())
                .map(existing -> {
                    existing.setName(finalUserToSave.getName());
                    existing.setEmail(finalUserToSave.getEmail());
                    existing.setFirstName(finalUserToSave.getFirstName());
                    existing.setLastName(finalUserToSave.getLastName());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(finalUserToSave));

        audit("CREATE_USER");
        return saved;
    }

    private void audit(@NonNull String action) {
        CurrentUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        AuditEvent auditEvent = AuditEvent.builder()
                .serviceName(UserService.class.getSimpleName())
                .actorUserId(currentUser.getId())
                .actorUsername(currentUser.getName())
                .action(action)
                .result(true)
                .exception(null)
                .build();

        auditService.audit(auditEvent);
    }
}
