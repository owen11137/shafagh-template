package com.misc.shafagh.cif.impl.validation;

import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import org.sonatype.aether.transfer.TransferEvent;

/*
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class RequiresSsnByTypeValidator implements ConstraintValidator<RequiresSsnByType, Object[]> {

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext context) {
        TransferEvent.RequestType requestType = (TransferEvent.RequestType) values[0];
        String ssn = (String) values[1];
        return !Strings.isNullOrEmpty(ssn) || requestType.equals(RequestType.BRANCH);
    }

}*/
