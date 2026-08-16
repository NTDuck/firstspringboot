package com.viettelsoftware.firstspringboot.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TaskDescriptionValidatorTest {

    private TaskDescriptionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TaskDescriptionValidator();
    }

    @Test
    void testNullDescription() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void testBlankDescription() {
        assertFalse(validator.isValid("", null));
        assertFalse(validator.isValid("   ", null));
    }

    @Test
    void testLeadingOrTrailingWhitespace() {
        assertFalse(validator.isValid(" test", null));
        assertFalse(validator.isValid("test ", null));
        assertFalse(validator.isValid(" test ", null));
        assertFalse(validator.isValid("\ttest", null));
        assertFalse(validator.isValid("test\n", null));
    }

    @Test
    void testExceedsLength() {
        String longDescription = "a".repeat(256);
        assertFalse(validator.isValid(longDescription, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Valid description", "a", "Task 123", "Hello World!"})
    void testValidDescription(String input) {
        assertTrue(validator.isValid(input, null));
    }

    @Test
    void testExact255Length() {
        String exact255 = "a".repeat(255);
        assertTrue(validator.isValid(exact255, null));
    }
}
