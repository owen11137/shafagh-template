package com.misc.shafagh.cif.impl.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StringFieldValidator implements ConstraintValidator<StringField, String> {

    private Boolean notEmpty;
    private Integer min;
    private Integer max;
    private String message;
    private String messageSize;

    @Override
    public void initialize(StringField field) {
        notEmpty = field.notEmpty();
        min = field.min();
        max = field.max();
        message = field.message();
        messageSize = field.messageLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean isEmpty = notEmpty && (value == null || value.isEmpty());
        if (isEmpty) {
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        boolean isInvalid = (min > 0 || max < Integer.MAX_VALUE) && (value.length() < min || value.length() > max);

        if (isInvalid) {
            context.buildConstraintViolationWithTemplate(messageSize).addConstraintViolation();
            return false;
        }
        return true;
    }
}