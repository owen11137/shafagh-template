package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.misc.shafagh.cif.api.dto.PersonDto;
import com.misc.shafagh.dpst.api.dto.DepositDto;
import com.misc.shafagh.loan.api.dto.LoanDto;
import com.misc.shafagh.orchestration.LoanApplicationService;
import com.misc.shafagh.orchestration.OpenLoanCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * سناریوی «یا همه یا هیچ»: ثبت تسهیلات = بررسی شخص (CIF) + بلوکه‌ی وثیقه (DPST) + ثبت تسهیلات (LOAN).
 * هر تست در پایان وضعیت واقعی دیتابیس را (نه فقط پاسخ سرویس) بررسی می‌کند.
 */
class MultiModuleTransactionTest extends AbstractScenarioSupport {

    @Autowired LoanApplicationService loanApplicationService;
    @Autowired TestScenarioService testScenarioService;

    private OpenLoanCommand command(PersonDto person, DepositDto deposit, String collateral, String loanAmount) {
        return new OpenLoanCommand(person.id(), deposit.id(), bd(collateral), bd(loanAmount));
    }

    // ---------------- موفقیت ----------------

    @Test
    void allStepsSucceed_everythingIsCommitted_inEachModuleSchema() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        LoanDto loan = loanApplicationService.openLoanWithCollateral(command(person, deposit, "400", "500"));

        assertNotNull(loan.id());
        assertAmount("400", blockedInDb(deposit.id()));
        assertEquals(1, loansInDb(deposit.id()));
        // ردیف‌ها واقعاً در اسکیمای هر ماژول هستند
        assertEquals(1, rowCount(T_PERSON, person.id()));
        assertEquals(1, rowCount(T_DEPOSIT, deposit.id()));
        assertEquals(1, rowCount(T_LOAN, loan.id()));
    }

    // ---------------- شکست در هر مرحله = rollback همه ----------------

    @Test
    void failureInStep1_personNotActive_nothingChanges() {
        PersonDto owner = newPerson();
        PersonDto inactive = newPerson();
        deactivate(inactive);
        DepositDto deposit = newDeposit(owner, "1000");

        assertBusinessError("PERSON_NOT_ACTIVE", () ->
                loanApplicationService.openLoanWithCollateral(command(inactive, deposit, "400", "500")));

        assertAmount("0", blockedInDb(deposit.id()));
        assertEquals(0, loansInDb(deposit.id()));
    }

    @Test
    void failureInStep2_insufficientFunds_nothingChanges() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        assertBusinessError("DEPOSIT_INSUFFICIENT_FUNDS", () ->
                loanApplicationService.openLoanWithCollateral(command(person, deposit, "5000", "500")));

        assertAmount("0", blockedInDb(deposit.id()));
        assertEquals(0, loansInDb(deposit.id()));
    }

    @Test
    void failureInStep2_invalidCollateral_nothingChanges() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        assertBusinessError("DEPOSIT_INVALID_AMOUNT", () ->
                loanApplicationService.openLoanWithCollateral(command(person, deposit, "0", "500")));

        assertAmount("0", blockedInDb(deposit.id()));
        assertEquals(0, loansInDb(deposit.id()));
    }

    @Test
    void failureInStep3_invalidLoanAmount_blockedAmountOfDepositModuleIsRolledBack() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        // مرحله‌ی ۲ (بلوکه) موفق است، مرحله‌ی ۳ (ثبت تسهیلات با مبلغ منفی) خطا می‌دهد
        assertBusinessError("LOAN_INVALID_AMOUNT", () ->
                loanApplicationService.openLoanWithCollateral(command(person, deposit, "400", "-1")));

        assertAmount("0", blockedInDb(deposit.id()));
        assertEquals(0, loansInDb(deposit.id()));
    }

    @Test
    void failureInStep3_depositOwnedBySomeoneElse_blockedAmountIsRolledBack() {
        PersonDto owner = newPerson();
        PersonDto stranger = newPerson();
        DepositDto deposit = newDeposit(owner, "1000");

        assertBusinessError("LOAN_DEPOSIT_OWNER_MISMATCH", () ->
                loanApplicationService.openLoanWithCollateral(command(stranger, deposit, "400", "500")));

        assertAmount("0", blockedInDb(deposit.id()));
        assertEquals(0, loansInDb(deposit.id()));
    }

    // ---------------- موفقیت قبلی با شکست بعدی ----------------

    @Test
    void earlierCommittedWorkSurvives_whenALaterOperationFails() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        loanApplicationService.openLoanWithCollateral(command(person, deposit, "400", "500"));

        // دومی: موجودی آزاد فقط ۶۰۰ است، پس مرحله‌ی ۲ خطا می‌دهد
        assertBusinessError("DEPOSIT_INSUFFICIENT_FUNDS", () ->
                loanApplicationService.openLoanWithCollateral(command(person, deposit, "700", "300")));

        assertAmount("400", blockedInDb(deposit.id()));
        assertEquals(1, loansInDb(deposit.id()));
    }

    // ---------------- الگوهای اشتباه (چرا قوانین راهنما وجود دارند) ----------------

    @Test
    void checkedException_withRollbackForException_rollsBackEverything() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        assertThrows(Exception.class, () ->
                testScenarioService.blockThenFailWithCheckedException(deposit.id(), bd("300")));

        assertAmount("0", blockedInDb(deposit.id()));
    }

    @Test
    void checkedException_withoutRollbackFor_COMMITS_theChanges() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        assertThrows(Exception.class, () ->
                testScenarioService.blockThenFailWithCheckedExceptionWithoutRollbackFor(deposit.id(), bd("300")));

        // به همین دلیل در orchestration همیشه rollbackFor = Exception.class می‌گذاریم
        assertAmount("300", blockedInDb(deposit.id()));
    }

    @Test
    void swallowingAModuleFailureInsideTheTransaction_endsInUnexpectedRollback() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        assertThrows(UnexpectedRollbackException.class, () ->
                testScenarioService.blockThenSwallowModuleFailure(person.id(), deposit.id(), bd("300")));

        assertAmount("0", blockedInDb(deposit.id()));
    }
}
