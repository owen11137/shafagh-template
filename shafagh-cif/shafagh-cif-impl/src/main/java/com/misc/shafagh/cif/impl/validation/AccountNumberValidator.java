package com.misc.shafagh.cif.impl.validation;

import com.misc.client.infra.starter.v3.validation.base.BaseValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;


public class AccountNumberValidator implements BaseValidator<AccountNumber, String> {

    @Override
    public void initialize(AccountNumber accountNumber) {
    }

    @Override
    public boolean isValid(String accountNo, ConstraintValidatorContext constraintValidatorContext) {
        try {

            if (accountNo == null) {
                return false;
            }

            if (StringUtils.isEmpty(accountNo)) {
                return false;
            }

            if (accountNo.length() == 26) {
                accountNo = accountNo.substring(accountNo.length() - 13);
            }

            if (accountNo.length() != 13) {
                return false;
            }

            return StringUtils.isNumeric(accountNo);
        } catch (Exception e) {
            return false;
        }
    }
}