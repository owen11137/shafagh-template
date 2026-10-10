package com.misc.shafagh.cif.impl.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;



public class AccountNumberValidator implements ConstraintValidator<AccountNumber, String> {

    @Override
    public void initialize(AccountNumber accountNumber) {
    }

    @Override
    public boolean isValid(String accountNo, ConstraintValidatorContext constraintValidatorContext) {
        try {

            if (accountNo == null) {
                return false;
            }

            if (accountNo.isEmpty()) {
                return false;
            }

            if (accountNo.length() == 26) {
                accountNo = accountNo.substring(accountNo.length() - 13);
            }

            if (accountNo.length() != 13) {
                return false;
            }

            return accountNo.chars().allMatch(c -> c >= '0' && c <= '9');
        } catch (Exception e) {
            return false;
        }
    }
}