package com.misc.shafagh.dpst.impl.service.impl;
import com.misc.shafagh.dpst.impl.service.api.DepositOpeningService;
import com.misc.shafagh.cif.api.CustomerLookup;
import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.dpst.impl.dto.request.OpenDepositRequest;
import com.misc.shafagh.dpst.impl.dto.response.OpenDepositResponse;
import com.misc.shafagh.dpst.impl.entity.*;
import com.misc.shafagh.dpst.impl.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import com.misc.shafagh.common.context.CurrentUserContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
/** افتتاح حساب و ثبت مالک به‌صورت اتمیک در واحد تراکنش DPST. */
@Service
@RequiredArgsConstructor
public class DepositOpeningServiceImpl implements DepositOpeningService {
    private final CustomerLookup customers;
    private final ProductDepositRepository products;
    private final DepositRepository deposits;
    private final DepositPersonRepository persons;
    private final CurrentUserContext currentUserContext;

    @Transactional(transactionManager="dpstTransactionManager")
    public OpenDepositResponse open(OpenDepositRequest request) {
        long fundId = currentUserContext.fundId();
        if (request.nationalCode()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "شماره ملی معتبر نیست");
        Long customerId = customers.findActiveIndividualId(request.nationalCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "مشتری حقیقی فعال در صندوق پیدا نشد"));
        ProductDepositEntity product = products.findByCodeAndFundId(request.productCode(), fundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "محصول سپرده در صندوق پیدا نشد"));
        LocalDateTime now = LocalDateTime.now();
        if (product.getStatus() != ProductStatus.ACTIVE
                || (product.getFromDate()!=null && now.isBefore(product.getFromDate()))
                || (product.getToDate()!=null && !now.isBefore(product.getToDate())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "محصول سپرده فعال یا در بازه مجاز نیست");
        DepositEntity deposit = new DepositEntity();
        deposit.setFundId(fundId); deposit.setCustomerId(customerId);
        deposit.setProductCode(product.getCode()); deposit.setDepositStatus(DepositStatus.OPN);
        deposit.setBalance(BigDecimal.ZERO);
        deposit.setDescription(request.description());
        // SEQUENCE assigns the ID on persist; finalize before any flush/insert or further query.
        deposit.setDepositNumber("PENDING-" + UUID.randomUUID());
        deposit = deposits.save(deposit);
        deposit.setDepositNumber(String.format(java.util.Locale.ROOT, "%020d", deposit.getId()));
        DepositPersonEntity owner = new DepositPersonEntity();
        owner.setFundId(fundId); owner.setCustomerId(customerId); owner.setDeposit(deposit);
        owner.setDepositPersonType(DepositPersonType.OWNER); owner.setFromDate(now);
        persons.saveAndFlush(owner);
        return new OpenDepositResponse(deposit.getId(), deposit.getDepositNumber(), customerId,
                product.getCode(), deposit.getDepositStatus(), deposit.getBalance(), deposit.getDescription());
    }
    
}
