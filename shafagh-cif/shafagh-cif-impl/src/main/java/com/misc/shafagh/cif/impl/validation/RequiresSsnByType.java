package com.misc.shafagh.cif.impl.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/*Constraint(validatedBy = RequiresSsnByTypeValidator.class)
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface RequiresSsnByType {

    String message() default "misc.exceptions.validation.2005";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}*/