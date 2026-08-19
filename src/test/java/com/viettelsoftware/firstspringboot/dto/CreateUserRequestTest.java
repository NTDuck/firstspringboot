package com.viettelsoftware.firstspringboot.dto;

import com.viettelsoftware.firstspringboot.controller.model.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUserRequest() {
        CreateUserRequest req = CreateUserRequest.builder()
                .keycloakId("kc-123")
                .name("validuser")
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testShortUsername() {
        CreateUserRequest req = CreateUserRequest.builder()
                .keycloakId("kc-123")
                .name("ab")
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testInvalidEmail() {
        CreateUserRequest req = CreateUserRequest.builder()
                .keycloakId("kc-123")
                .name("validuser")
                .email("invalid-email-format")
                .firstName("John")
                .lastName("Doe")
                .build();

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testLongFieldsExceeding255() {
        String longStr = "a".repeat(256);
        CreateUserRequest req = CreateUserRequest.builder()
                .keycloakId("kc-123")
                .name(longStr)
                .email("user@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
    }
}
