package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.misc.shafagh.base.api.dto.CurrencyDto;
import com.misc.shafagh.cif.api.dto.CreatePersonRequest;
import com.misc.shafagh.cif.api.dto.PersonDto;
import com.misc.shafagh.dpst.api.dto.DepositDto;
import com.misc.shafagh.dpst.api.dto.OpenDepositRequest;
import com.misc.shafagh.loan.api.dto.RegisterLoanRequest;
import org.junit.jupiter.api.Test;

/**
 * ارتباط ماژول‌ها فقط از طریق سرویس‌های expose شده و اعتبارسنجی‌های هر ماژول (بدون orchestration).
 */
class ModuleCollaborationTest extends AbstractScenarioSupport {

    // ---------------- CIF ----------------

    @Test
    void person_isStoredInCifSchema_andDuplicateNationalCodeIsRejected() {
        PersonDto person = newPerson();
        assertEquals(1, rowCount(T_PERSON, person.id()));

        assertBusinessError("PERSON_DUPLICATE", () ->
                personService.create(new CreatePersonRequest(person.nationalCode(), "Other", "Name")));
    }

    @Test
    void person_getByIdOfUnknownIdFails() {
        assertBusinessError("PERSON_NOT_FOUND", () -> personService.getById(987654321L));
    }

    // ---------------- BASE ----------------

    @Test
    void currency_duplicateIsRejected_andUnknownCodeFails() {
        assertBusinessError("CURRENCY_DUPLICATE", () -> currencyService.create(new CurrencyDto(CURRENCY, "Again")));
        assertBusinessError("CURRENCY_NOT_FOUND", () -> currencyService.getByCode("ZZZ"));
    }

    // ---------------- DPST (به CIF و BASE وابسته است) ----------------

    @Test
    void deposit_open_succeedsForActivePersonAndKnownCurrency_andIsStoredInDpstSchema() {
        PersonDto person = newPerson();

        DepositDto deposit = newDeposit(person, "1000");

        assertEquals(1, rowCount(T_DEPOSIT, deposit.id()));
        assertAmount("0", blockedInDb(deposit.id()));
    }

    @Test
    void deposit_open_failsForUnknownPerson() {
        assertBusinessError("PERSON_NOT_ACTIVE", () ->
                depositService.open(new OpenDepositRequest(987654321L, CURRENCY, bd("10"))));
    }

    @Test
    void deposit_open_failsForInactivePerson() {
        PersonDto person = newPerson();
        deactivate(person);

        assertBusinessError("PERSON_NOT_ACTIVE", () ->
                depositService.open(new OpenDepositRequest(person.id(), CURRENCY, bd("10"))));
    }

    @Test
    void deposit_open_failsForUnknownCurrency() {
        PersonDto person = newPerson();

        assertBusinessError("CURRENCY_NOT_FOUND", () ->
                depositService.open(new OpenDepositRequest(person.id(), "ZZZ", bd("10"))));
    }

    @Test
    void deposit_open_failsForNegativeInitialBalance() {
        PersonDto person = newPerson();

        assertBusinessError("DEPOSIT_INVALID_BALANCE", () ->
                depositService.open(new OpenDepositRequest(person.id(), CURRENCY, bd("-1"))));
    }

    @Test
    void deposit_blockAmount_validations() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        assertBusinessError("DEPOSIT_INVALID_AMOUNT", () -> depositService.blockAmount(deposit.id(), bd("0")));
        assertBusinessError("DEPOSIT_INVALID_AMOUNT", () -> depositService.blockAmount(deposit.id(), bd("-5")));
        assertBusinessError("DEPOSIT_INSUFFICIENT_FUNDS", () -> depositService.blockAmount(deposit.id(), bd("1001")));
        assertBusinessError("DEPOSIT_NOT_FOUND", () -> depositService.blockAmount(987654321L, bd("10")));
        assertAmount("0", blockedInDb(deposit.id()));
    }

    @Test
    void deposit_blockAmount_isCumulative_andNeverExceedsBalance() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        depositService.blockAmount(deposit.id(), bd("300"));
        depositService.blockAmount(deposit.id(), bd("400"));
        assertAmount("700", blockedInDb(deposit.id()));

        assertBusinessError("DEPOSIT_INSUFFICIENT_FUNDS", () -> depositService.blockAmount(deposit.id(), bd("400")));
        assertAmount("700", blockedInDb(deposit.id()));
    }

    // ---------------- LOAN (به CIF و DPST وابسته است) ----------------

    @Test
    void loan_register_succeedsAndIsStoredInLoanSchema() {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");

        var loan = loanService.register(new RegisterLoanRequest(person.id(), deposit.id(), bd("500")));

        assertEquals(1, rowCount(T_LOAN, loan.id()));
        assertEquals(1, loansInDb(deposit.id()));
    }

    @Test
    void loan_register_validations() {
        PersonDto owner = newPerson();
        PersonDto stranger = newPerson();
        DepositDto deposit = newDeposit(owner, "1000");

        assertBusinessError("LOAN_INVALID_AMOUNT", () ->
                loanService.register(new RegisterLoanRequest(owner.id(), deposit.id(), bd("0"))));
        assertBusinessError("LOAN_INVALID_AMOUNT", () ->
                loanService.register(new RegisterLoanRequest(owner.id(), deposit.id(), bd("-1"))));
        assertBusinessError("PERSON_NOT_ACTIVE", () ->
                loanService.register(new RegisterLoanRequest(987654321L, deposit.id(), bd("10"))));
        assertBusinessError("DEPOSIT_NOT_FOUND", () ->
                loanService.register(new RegisterLoanRequest(owner.id(), 987654321L, bd("10"))));
        assertBusinessError("LOAN_DEPOSIT_OWNER_MISMATCH", () ->
                loanService.register(new RegisterLoanRequest(stranger.id(), deposit.id(), bd("10"))));
        assertEquals(0, loansInDb(deposit.id()));
    }

    @Test
    void loan_getByIdOfUnknownIdFails() {
        assertBusinessError("LOAN_NOT_FOUND", () -> loanService.getById(987654321L));
    }
}
