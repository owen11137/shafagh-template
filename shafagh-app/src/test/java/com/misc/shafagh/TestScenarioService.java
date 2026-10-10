package com.misc.shafagh;

import com.misc.shafagh.base.api.BusinessException;
import com.misc.shafagh.dpst.api.DepositService;
import com.misc.shafagh.loan.api.LoanService;
import com.misc.shafagh.loan.api.dto.RegisterLoanRequest;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * فقط برای تست (در src/test): الگوهای اشتباه رایج را عمداً پیاده می‌کند تا ثابت شود چرا
 * قوانین راهنمای برنامه‌نویسان وجود دارند. هرگز آن را در کد اصلی کپی نکنید.
 */
@Service
public class TestScenarioService {

    private final DepositService depositService;
    private final LoanService loanService;

    public TestScenarioService(DepositService depositService, LoanService loanService) {
        this.depositService = depositService;
        this.loanService = loanService;
    }

    /** الگوی درست: rollbackFor = Exception.class، پس exception از نوع checked هم rollback می‌کند. */
    @Transactional(rollbackFor = Exception.class)
    public void blockThenFailWithCheckedException(Long depositId, BigDecimal amount) throws Exception {
        depositService.blockAmount(depositId, amount);
        throw new Exception("checked failure");
    }

    /** الگوی اشتباه: بدون rollbackFor، exception از نوع checked باعث commit می‌شود! */
    @Transactional
    public void blockThenFailWithCheckedExceptionWithoutRollbackFor(Long depositId, BigDecimal amount) throws Exception {
        depositService.blockAmount(depositId, amount);
        throw new Exception("checked failure");
    }

    /** الگوی اشتباه: قورت‌دادن exception سرویس ماژول داخل تراکنش، نتیجه: UnexpectedRollbackException. */
    @Transactional
    public void blockThenSwallowModuleFailure(Long personId, Long depositId, BigDecimal amount) {
        depositService.blockAmount(depositId, amount);
        try {
            loanService.register(new RegisterLoanRequest(personId, depositId, new BigDecimal("-1")));
        } catch (BusinessException ignored) {
            // عمداً قورت داده می‌شود
        }
    }
}
