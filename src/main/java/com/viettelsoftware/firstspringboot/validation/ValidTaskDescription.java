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
    String message() default "Invalid task description (must be `@NotBlank + @Size(max = 255) + @Pattern(regexp = \"^\\\\S(?:.*\\\\S)?$\")`)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
