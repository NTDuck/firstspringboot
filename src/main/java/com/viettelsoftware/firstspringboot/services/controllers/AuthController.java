package com.viettelsoftware.firstspringboot.services.controllers;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AuthController {
    @GetMapping("/me")
    public UserInfoDto getGretting(JwtAuthenticationToken auth) {
        return new UserInfoDto(
                auth.getToken().getClaimAsString(StandardClaimNames.PREFERRED_USERNAME),
                auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }

    public static class UserInfoDto {
        String name;
        List roles;

        public UserInfoDto(String name, List roles) {
            this.name = name;
            this.roles = roles;
        }
    }

//    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
//    private @NonNull String issuerUri;
//
//    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<@NonNull ?> loginWithJson(@Valid @RequestBody @NonNull LoginRequest request) {
//        return authenticate(request.getUsername(), request.getPassword());
//    }
//
//    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//    public ResponseEntity<@NonNull ?> loginWithForm(@RequestParam @NonNull String username, @RequestParam @NonNull String password) {
//        return authenticate(username, password);
//    }
//
//    private ResponseEntity<@NonNull ?> authenticate(@NonNull String username, @NonNull String password) {
//        val tokenUrl = issuerUri + "/protocol/openid-connect/token";
//
//        val restTemplate = new RestTemplate();
//        val headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        val map = new LinkedMultiValueMap<String, String>();
//        map.add("grant_type", "password");
//        map.add("client_id", "firstspringboot-client");
//        map.add("username", username);
//        map.add("password", password);
//
//        val request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
//
//        try {
//            val response = restTemplate.postForEntity(tokenUrl, request, String.class);
//            return ResponseEntity.status(response.getStatusCode())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(response.getBody());
//        } catch (HttpClientErrorException e) {
//            return ResponseEntity.status(e.getStatusCode())
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(e.getResponseBodyAsString());
//        }
//    }
//
//    @Getter
//    @Setter
//    public static class LoginRequest {
//        private @NonNull String username;
//        private @NonNull String password;
//    }
//
//    public enum Role {
//        GET,
//        POST,
//        PUT,
//        DELETE,
//    }
//
//    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
//    public static class MethodNotAllowedException extends Exception {
//        private static final long serialVersionUID = 1L;
//
//        public static @NonNull MethodNotAllowedException of(@NonNull String username, @NonNull Role role) {
//            return new MethodNotAllowedException("User " + username + " not allowed access to " + role);
//        }
//
//        private MethodNotAllowedException(@NonNull String message) {
//            super(message);
//        }
//    }
}
