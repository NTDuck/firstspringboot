package com.viettelsoftware.firstspringboot.services.controllers;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;

@RestController
public class LoginController {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private @NonNull String issuerUri;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<@NonNull ?> loginWithJson(@Valid @RequestBody @NonNull LoginRequest request) {
        return authenticate(request.getUsername(), request.getPassword());
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<@NonNull ?> loginWithForm(@RequestParam @NonNull String username, @RequestParam @NonNull String password) {
        return authenticate(username, password);
    }

    private ResponseEntity<@NonNull ?> authenticate(@NonNull String username, @NonNull String password) {
        val tokenUrl = issuerUri + "/protocol/openid-connect/token";

        val restTemplate = new RestTemplate();
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        val map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "password");
        map.add("client_id", "firstspringboot-client");
        map.add("username", username);
        map.add("password", password);

        val request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        try {
            val response = restTemplate.postForEntity(tokenUrl, request, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        }
    }

    @Getter
    @Setter
    public static class LoginRequest {
        private @NonNull String username;
        private @NonNull String password;
    }
}
