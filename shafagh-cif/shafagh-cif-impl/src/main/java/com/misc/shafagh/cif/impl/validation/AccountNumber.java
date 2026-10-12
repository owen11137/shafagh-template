package com.misc.shafagh.cif.impl.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = {AccountNumberValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Documented
public @interface AccountNumber {
    Class<? extends Payload>[] payload() default {};

    Class<?>[] groups() default {};

    String message() default "misc.exceptions.validation.2004";
}
