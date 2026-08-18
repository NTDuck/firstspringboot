package com.viettelsoftware.firstspringboot.scheduler;

import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import lombok.NonNull;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakUserSyncCronjob {

    private final @NonNull Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserRepository userRepository;

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
            logger.info("Starting Keycloak user synchronization...");
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(adminRealm)
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId(adminClientId)
                    .build();

            List<UserRepresentation> keycloakUsers = keycloak.realm(targetRealm).users().list();
            if (keycloakUsers == null || keycloakUsers.isEmpty()) {
                logger.warn("No Keycloak users returned or error occurred during fetch.");
                return;
            }

            for (UserRepresentation userRep : keycloakUsers) {
                String keycloakId = userRep.getId();
                if (keycloakId == null || keycloakId.isBlank()) {
                    continue;
                }

                String username = userRep.getUsername() != null ? userRep.getUsername() : "";
                String email = userRep.getEmail() != null ? userRep.getEmail() : "";
                String firstName = userRep.getFirstName() != null ? userRep.getFirstName() : "";
                String lastName = userRep.getLastName() != null ? userRep.getLastName() : "";
                String name = !username.isBlank() ? username : (firstName + " " + lastName).trim();

                User user = User.builder()
                        .keycloakId(keycloakId)
                        .name(name)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();

                userRepository.findByKeycloakId(keycloakId)
                        .map(existing -> {
                            existing.setName(user.getName());
                            existing.setEmail(user.getEmail());
                            existing.setFirstName(user.getFirstName());
                            existing.setLastName(user.getLastName());
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> userRepository.save(user));
            }
            logger.info("Keycloak user synchronization completed successfully. Synced {} users.", keycloakUsers.size());
        } catch (Exception exception) {
            logger.error("Error occurred while syncing Keycloak users: {}", exception.getMessage(), exception);
        }
    }
}
