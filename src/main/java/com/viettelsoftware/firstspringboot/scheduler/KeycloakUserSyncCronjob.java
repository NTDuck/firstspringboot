package com.viettelsoftware.firstspringboot.scheduler;

import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.service.UserService;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class KeycloakUserSyncCronjob {

    private final @NonNull Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

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

    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "${keycloak.sync.cron:0 */5 * * * *}")
    public void syncKeycloakUsers() {
        try {
            logger.info("Starting Keycloak user synchronization...");
            String token = getAdminAccessToken();
            if (token == null || token.isBlank()) {
                logger.warn("Failed to obtain Keycloak admin access token.");
                return;
            }

            List<Map<String, Object>> keycloakUsers = fetchKeycloakUsers(token);
            if (keycloakUsers == null || keycloakUsers.isEmpty()) {
                logger.warn("No Keycloak users returned or error occurred during fetch.");
                return;
            }

            for (Map<String, Object> userMap : keycloakUsers) {
                String keycloakId = extractString(userMap, "id");
                if (keycloakId.isBlank()) {
                    continue;
                }

                String username = extractString(userMap, "username");
                String email = extractString(userMap, "email");
                String firstName = extractString(userMap, "firstName");
                String lastName = extractString(userMap, "lastName");
                String name = !username.isBlank() ? username : (firstName + " " + lastName).trim();

                User user = User.builder()
                        .keycloakId(keycloakId)
                        .name(name)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();

                userService.createUser(user);
            }
            logger.info("Keycloak user synchronization completed successfully. Synced {} users.", keycloakUsers.size());
        } catch (Exception e) {
            logger.error("Error occurred while syncing Keycloak users: {}", e.getMessage(), e);
        }
    }

    private String extractString(Map<String, Object> map, String key) {
        Object val = map.getOrDefault(key, "");
        return val != null ? val.toString() : "";
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakServerUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", adminClientId);
        map.add("username", adminUsername);
        map.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Object tokenObj = response.getBody().get("access_token");
            return tokenObj != null ? tokenObj.toString() : null;
        }
        return null;
    }

    private List<Map<String, Object>> fetchKeycloakUsers(String token) {
        String usersUrl = keycloakServerUrl + "/admin/realms/" + targetRealm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                usersUrl, HttpMethod.GET, request, new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }
        return List.of();
    }
}
