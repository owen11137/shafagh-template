package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.misc.shafagh.cif.api.dto.PersonDto;
import com.misc.shafagh.dpst.api.dto.DepositDto;
import com.misc.shafagh.orchestration.LoanApplicationService;
import com.misc.shafagh.orchestration.OpenLoanCommand;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * دو درخواست هم‌زمان که هر کدام ۶۰۰ از موجودی ۱۰۰۰ را بلوکه می‌کنند:
 * فقط یکی باید موفق شود (قفل خوش‌بینانه‌ی DepositEntity) و دیگری کاملاً rollback شود.
 */
class ConcurrencyTest extends AbstractScenarioSupport {

    @Autowired LoanApplicationService loanApplicationService;

    @Test
    void twoConcurrentBlocksBeyondBalance_onlyOneSucceeds_andNothingIsLeftBehindByTheLoser() throws Exception {
        PersonDto person = newPerson();
        DepositDto deposit = newDeposit(person, "1000");
        OpenLoanCommand command = new OpenLoanCommand(person.id(), deposit.id(), bd("600"), bd("100"));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch start = new CountDownLatch(1);
            Callable<Boolean> task = () -> {
                start.await();
                try {
                    loanApplicationService.openLoanWithCollateral(command);
                    return true;
                } catch (Exception expectedForTheLoser) {
                    return false;
                }
            };
            Future<Boolean> first = pool.submit(task);
            Future<Boolean> second = pool.submit(task);
            start.countDown();

            int successes = (first.get(60, TimeUnit.SECONDS) ? 1 : 0) + (second.get(60, TimeUnit.SECONDS) ? 1 : 0);

            assertEquals(1, successes, "exactly one concurrent request must succeed");
            assertAmount("600", blockedInDb(deposit.id()));
            assertEquals(1, loansInDb(deposit.id()));
        } finally {
            pool.shutdownNow();
        }
    }
}
