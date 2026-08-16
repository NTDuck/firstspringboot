package com.viettelsoftware.firstspringboot.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TaskDescriptionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTaskDescription {
    String message() default "Task description must not be blank, must be at most 255 characters, and must not begin or end with whitespace";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
