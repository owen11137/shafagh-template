package com.misc.shafagh.orchestration;

import com.misc.shafagh.base.api.BusinessException;
import com.misc.shafagh.cif.api.PersonService;
import com.misc.shafagh.dpst.api.DepositService;
import com.misc.shafagh.loan.api.LoanService;
import com.misc.shafagh.loan.api.dto.LoanDto;
import com.misc.shafagh.loan.api.dto.RegisterLoanRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * نمونه‌ی سناریوی چندماژولی: ثبت تسهیلات همراه با بلوکه‌کردن وثیقه روی سپرده.
 *
 * مرز تراکنش فقط همین‌جاست. اگر هر مرحله خطا بدهد، تغییرات همه‌ی ماژول‌ها
 * (CIF/DPST/LOAN) با هم rollback می‌شود. این کلاس فقط به اینترفیس‌های API وابسته است.
 *
 * هر سناریوی چندماژولی جدید را به همین شکل در این ماژول اضافه کنید.
 */
@Service
public class LoanApplicationService {

    private final PersonService personService;
    private final DepositService depositService;
    private final LoanService loanService;

    public LoanApplicationService(PersonService personService, DepositService depositService, LoanService loanService) {
        this.personService = personService;
        this.depositService = depositService;
        this.loanService = loanService;
    }

    @Transactional(rollbackFor = Exception.class)
    public LoanDto openLoanWithCollateral(OpenLoanCommand command) {
        if (!personService.existsActive(command.personId())) {
            throw new BusinessException("PERSON_NOT_ACTIVE", "Person is not active: " + command.personId());
        }
        depositService.blockAmount(command.depositId(), command.collateralAmount());
        return loanService.register(
                new RegisterLoanRequest(command.personId(), command.depositId(), command.loanAmount()));
    }
}
