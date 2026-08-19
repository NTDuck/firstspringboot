package com.viettelsoftware.firstspringboot.scheduler;

import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakUserSyncCronjob {

    private final UserRepository userRepository;

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

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "${keycloak.sync.cron:0 */5 * * * *}")
    public void syncKeycloakUsers() {
        try {
            log.info("Starting Keycloak user synchronization...");
            val keycloakUsers = fetchKeycloakUsers();
            if (keycloakUsers.isEmpty()) {
                log.warn("No Keycloak users returned or error occurred during fetch.");
                return;
            }

            syncUsers(keycloakUsers);
            log.info("Keycloak user synchronization completed successfully. Synced {} users.", keycloakUsers.size());
        } catch (Exception exception) {
            log.error("Error occurred while syncing Keycloak users: {}", exception.getMessage(), exception);
        }
    }

    private List<UserRepresentation> fetchKeycloakUsers() {
        val keycloak = buildKeycloakClient();
        val users = keycloak.realm(targetRealm).users().list();
        return users != null ? users : List.of();
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

    private void syncUsers(List<UserRepresentation> keycloakUsers) {
        for (val userRep : keycloakUsers) {
            syncUser(userRep);
        }
    }

    private void syncUser(UserRepresentation userRep) {
        val userOptional = buildUserFromRepresentation(userRep);
        if (userOptional.isEmpty()) return;

        saveOrUpdateUser(userOptional.get());
    }

    private Optional<User> buildUserFromRepresentation(UserRepresentation userRep) {
        val keycloakId = userRep.getId();
        if (keycloakId == null || keycloakId.isBlank()) return Optional.empty();

        val username = userRep.getUsername() != null ? userRep.getUsername() : "";
        val email = userRep.getEmail() != null ? userRep.getEmail() : "";
        val firstName = userRep.getFirstName() != null ? userRep.getFirstName() : "";
        val lastName = userRep.getLastName() != null ? userRep.getLastName() : "";
        val name = !username.isBlank() ? username : (firstName + " " + lastName).trim();

        val user = User.builder()
                .keycloakId(keycloakId)
                .name(name)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();

        return Optional.of(user);
    }

    private void saveOrUpdateUser(User user) {
        userRepository.findByKeycloakId(user.getKeycloakId())
                .map(existing -> {
                    existing.setName(user.getName());
                    existing.setEmail(user.getEmail());
                    existing.setFirstName(user.getFirstName());
                    existing.setLastName(user.getLastName());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(user));
    }
}
