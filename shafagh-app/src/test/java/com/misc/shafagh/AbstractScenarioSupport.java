package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.misc.shafagh.base.api.BusinessException;
import com.misc.shafagh.base.api.CurrencyService;
import com.misc.shafagh.base.api.dto.CurrencyDto;
import com.misc.shafagh.base.impl.BaseSchema;
import com.misc.shafagh.cif.api.PersonService;
import com.misc.shafagh.cif.api.dto.CreatePersonRequest;
import com.misc.shafagh.cif.api.dto.PersonDto;
import com.misc.shafagh.cif.impl.CifSchema;
import com.misc.shafagh.dpst.api.DepositService;
import com.misc.shafagh.dpst.api.dto.DepositDto;
import com.misc.shafagh.dpst.api.dto.OpenDepositRequest;
import com.misc.shafagh.dpst.impl.DpstSchema;
import com.misc.shafagh.loan.api.LoanService;
import com.misc.shafagh.loan.impl.LoanSchema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * پایه‌ی تست‌های سناریو. همین تست‌ها هم روی H2 (پیش‌فرض) و هم روی Oracle واقعی
 * (-Dtest.profile=oracle) اجرا می‌شوند.
 *
 * - عمداً @Transactional نیست: هر فراخوانی سرویس تراکنش واقعی خودش را commit/rollback می‌کند.
 * - وضعیت دیتابیس با JdbcTemplate (اتصال جدا و بدون تراکنش) خوانده می‌شود، پس فقط داده‌ی commit شده را می‌بیند.
 * - هر تست داده‌های خودش را بعد از اجرا پاک می‌کند (مهم برای Oracle واقعی).
 */
@SpringBootTest
abstract class AbstractScenarioSupport {

    protected static final String CURRENCY = "TST";

    protected static final String T_CURRENCY = BaseSchema.NAME + ".SHFQ_BASE_CURRENCY";
    protected static final String T_PERSON = CifSchema.NAME + ".SHFQ_CIF_PERSON";
    protected static final String T_DEPOSIT = DpstSchema.NAME + ".SHFQ_DPST_DEPOSIT";
    protected static final String T_LOAN = LoanSchema.NAME + ".SHFQ_LOAN_LOAN";

    @Autowired protected CurrencyService currencyService;
    @Autowired protected PersonService personService;
    @Autowired protected DepositService depositService;
    @Autowired protected LoanService loanService;
    @Autowired protected DataSource dataSource;

    protected JdbcTemplate jdbc;
    private final List<Long> personIds = new ArrayList<>();

    @BeforeEach
    void prepareBaseData() {
        jdbc = new JdbcTemplate(dataSource);
        if (!currencyService.exists(CURRENCY)) {
            currencyService.create(new CurrencyDto(CURRENCY, "Test currency"));
        }
    }

    @AfterEach
    void cleanUpTestData() {
        for (Long personId : personIds) {
            jdbc.update("DELETE FROM " + T_LOAN + " WHERE PERSON_ID = ? OR DEPOSIT_ID IN "
                    + "(SELECT ID FROM " + T_DEPOSIT + " WHERE PERSON_ID = ?)", personId, personId);
            jdbc.update("DELETE FROM " + T_DEPOSIT + " WHERE PERSON_ID = ?", personId);
            jdbc.update("DELETE FROM " + T_PERSON + " WHERE ID = ?", personId);
        }
        personIds.clear();
        jdbc.update("DELETE FROM " + T_CURRENCY + " WHERE CODE = ?", CURRENCY);
    }

    // ---------- ساخت داده ----------

    protected static String uniqueNationalCode() {
        return "IT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    protected PersonDto newPerson() {
        PersonDto person = personService.create(new CreatePersonRequest(uniqueNationalCode(), "Test", "User"));
        personIds.add(person.id());
        return person;
    }

    protected DepositDto newDeposit(PersonDto owner, String balance) {
        return depositService.open(new OpenDepositRequest(owner.id(), CURRENCY, bd(balance)));
    }

    protected void deactivate(PersonDto person) {
        jdbc.update("UPDATE " + T_PERSON + " SET STATUS = 'INACTIVE' WHERE ID = ?", person.id());
    }

    // ---------- خواندن مستقیم از دیتابیس (فقط داده‌ی commit شده) ----------

    protected BigDecimal blockedInDb(Long depositId) {
        return jdbc.queryForObject("SELECT BLOCKED_AMOUNT FROM " + T_DEPOSIT + " WHERE ID = ?", BigDecimal.class, depositId);
    }

    protected int loansInDb(Long depositId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + T_LOAN + " WHERE DEPOSIT_ID = ?", Integer.class, depositId);
    }

    protected int rowCount(String table, Long id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE ID = ?", Integer.class, id);
    }

    // ---------- کمکی‌های assert ----------

    protected static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    protected static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    protected static BusinessException assertBusinessError(String expectedCode, Executable action) {
        BusinessException e = assertThrows(BusinessException.class, action);
        assertEquals(expectedCode, e.getCode());
        return e;
    }
}
