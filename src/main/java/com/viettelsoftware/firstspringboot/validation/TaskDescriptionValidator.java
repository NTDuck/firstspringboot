package com.viettelsoftware.firstspringboot.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TaskDescriptionValidator implements ConstraintValidator<ValidTaskDescription, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        if (value.trim().isEmpty()) {
            return false;
        }
        if (value.length() > 255) {
            return false;
        }
        if (Character.isWhitespace(value.charAt(0)) || Character.isWhitespace(value.charAt(value.length() - 1))) {
            return false;
        }
        return true;
    }
}
