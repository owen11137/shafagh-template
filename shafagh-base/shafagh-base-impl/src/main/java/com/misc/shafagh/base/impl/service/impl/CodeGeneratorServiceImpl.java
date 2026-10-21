package com.misc.shafagh.base.impl.service.impl;

import com.misc.shafagh.base.impl.service.api.CodeNumberStore;

import com.misc.shafagh.base.api.CodeGenerationException;
import com.misc.shafagh.base.api.CodeGenerationException.Reason;
import com.misc.shafagh.base.api.CodeGeneratorService;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    private static final Pattern FUND_CODE = Pattern.compile("[0-9]{6}");
    private static final Pattern BRANCH_CODE = Pattern.compile("[0-9]{11}");

    private final CodeNumberStore store;
    private final TransactionTemplate registrationTransaction;
    private final TransactionTemplate accountAllocationTransaction;

    public CodeGeneratorServiceImpl(
            CodeNumberStore store,
            @Qualifier("baseTransactionManager") PlatformTransactionManager transactionManager,
            @Value("${app.code-allocation.timeout-seconds:5}") int timeoutSeconds) {
        if (timeoutSeconds < 1) {
            throw new IllegalArgumentException("مهلت تخصیص شماره باید مثبت باشد");
        }
        this.store = Objects.requireNonNull(store);
        this.registrationTransaction = new TransactionTemplate(transactionManager);
        this.registrationTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
        this.accountAllocationTransaction = new TransactionTemplate(transactionManager);
        this.accountAllocationTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.accountAllocationTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.accountAllocationTransaction.setTimeout(timeoutSeconds);
        this.accountAllocationTransaction.setName("account-number-allocation");
    }

    @Override
    public String generateCustomerCode(String fundCode) {
        validateCode(fundCode, FUND_CODE, "کد صندوق باید دقیقاً ۶ رقم لاتین باشد");
        return format(fundCode, store.nextCustomerNumber(), 8, CodeNumberStore.MAX_CUSTOMER_NUMBER);
    }

    @Override
    public String generateBranchCode(String fundCode) {
        validateCode(fundCode, FUND_CODE, "کد صندوق باید دقیقاً ۶ رقم لاتین باشد");
        return registrationTransaction.execute(status -> {
            requireWritableTransaction();
            return format(fundCode, store.allocateBranchNumber(fundCode), 5, CodeNumberStore.MAX_BRANCH_NUMBER);
        });
    }

    @Override
    public String generateAccountNumber(String branchCode) {
        validateCode(branchCode, BRANCH_CODE, "کد شعبه باید دقیقاً ۱۱ رقم لاتین باشد");
        // TransactionTemplate commits before execute returns, including when this
        // service is called directly rather than through a Spring AOP proxy.
        return accountAllocationTransaction.execute(status -> {
            String payload = format(branchCode, store.allocateAccountNumber(branchCode),
                    6, CodeNumberStore.MAX_ACCOUNT_NUMBER);
            return payload + calculateLuhnCheckDigit(payload);
        });
    }

    @Override
    public void initializeBranchCounter(String fundCode) {
        validateCode(fundCode, FUND_CODE, "کد صندوق باید دقیقاً ۶ رقم لاتین باشد");
        registrationTransaction.executeWithoutResult(status -> {
            requireWritableTransaction();
            store.initializeBranchCounter(fundCode);
        });
    }

    @Override
    public void initializeAccountCounter(String branchCode) {
        validateCode(branchCode, BRANCH_CODE, "کد شعبه باید دقیقاً ۱۱ رقم لاتین باشد");
        registrationTransaction.executeWithoutResult(status -> {
            requireWritableTransaction();
            store.initializeAccountCounter(branchCode);
        });
    }

    private static void requireWritableTransaction() {
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalTransactionStateException("عملیات شمارنده به تراکنش قابل نوشتن نیاز دارد");
        }
    }

    private static void validateCode(String code, Pattern pattern, String message) {
        if (code == null || !pattern.matcher(code).matches()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String format(String prefix, long number, int width, long maximum) {
        if (number < 1) {
            throw new CodeGenerationException(Reason.INVALID_NUMBER_STATE, "مقدار تخصیص‌یافته باید مثبت باشد");
        }
        if (number > maximum) {
            throw new CodeGenerationException(Reason.CAPACITY_EXHAUSTED, "ظرفیت شماره‌گذاری پر شده است");
        }
        return prefix + String.format(Locale.ROOT, "%0" + width + "d", number);
    }

    private static int calculateLuhnCheckDigit(String digits) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
