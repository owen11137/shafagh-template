package com.misc.shafagh.cif.impl.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = StringFieldValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringField {

    String message() default "";

    String messageLength() default "";

    boolean notEmpty() default true;

    int min() default 0;

    int max() default Integer.MAX_VALUE;

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};
} 