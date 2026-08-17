package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.dto.CurrentUser;
import com.viettelsoftware.firstspringboot.entity.AuditEvent;
import com.viettelsoftware.firstspringboot.entity.User;
import com.viettelsoftware.firstspringboot.repository.UserRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
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

    private final RestTemplate restTemplate = new RestTemplate();

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
        String token = getAdminAccessToken();
        if (token != null && !token.isBlank()) {
            try {
                String usersUrl = keycloakServerUrl + "/admin/realms/" + targetRealm + "/users";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(token);

                Map<String, Object> body = Map.of(
                        "username", user.getName(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "enabled", true
                );

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(usersUrl, request, String.class);

                if (response.getStatusCode() == HttpStatus.CREATED && response.getHeaders().getLocation() != null) {
                    String path = response.getHeaders().getLocation().getPath();
                    String keycloakId = path.substring(path.lastIndexOf('/') + 1);
                    if (!keycloakId.isBlank()) {
                        userToSave = user.withKeycloakId(keycloakId);
                    }
                }
            } catch (Exception ignored) {
                // User may already exist in Keycloak or Keycloak unavailable, proceed to local DB save
            }
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

    private String getAdminAccessToken() {
        try {
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
        } catch (Exception ignored) {
        }
        return null;
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
