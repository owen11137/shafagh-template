package com.misc.shafagh.dpst.impl.service;

import com.misc.shafagh.base.api.BusinessException;
import com.misc.shafagh.base.api.CurrencyService;
import com.misc.shafagh.cif.api.PersonService;
import com.misc.shafagh.dpst.api.DepositService;
import com.misc.shafagh.dpst.api.dto.DepositDto;
import com.misc.shafagh.dpst.api.dto.OpenDepositRequest;
import com.misc.shafagh.dpst.impl.entity.DepositEntity;
import com.misc.shafagh.dpst.impl.repository.DepositRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DepositServiceImpl implements DepositService {

    private final DepositRepository repository;
    // ارتباط با ماژول‌های دیگر فقط از طریق اینترفیس‌های API آن‌ها
    private final PersonService personService;
    private final CurrencyService currencyService;

    DepositServiceImpl(DepositRepository repository, PersonService personService, CurrencyService currencyService) {
        this.repository = repository;
        this.personService = personService;
        this.currencyService = currencyService;
    }

    @Override
    @Transactional
    public DepositDto open(OpenDepositRequest request) {
        if (!personService.existsActive(request.personId())) {
            throw new BusinessException("PERSON_NOT_ACTIVE", "Person is not active: " + request.personId());
        }
        if (!currencyService.exists(request.currencyCode())) {
            throw new BusinessException("CURRENCY_NOT_FOUND", "Currency not found: " + request.currencyCode());
        }
        if (request.initialBalance().signum() < 0) {
            throw new BusinessException("DEPOSIT_INVALID_BALANCE", "Initial balance must not be negative");
        }
        return toDto(repository.save(
                new DepositEntity(request.personId(), request.currencyCode(), request.initialBalance())));
    }

    @Override
    public DepositDto getById(Long id) {
        return toDto(find(id));
    }

    @Override
    @Transactional
    public DepositDto blockAmount(Long depositId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("DEPOSIT_INVALID_AMOUNT", "Block amount must be positive");
        }
        DepositEntity deposit = find(depositId);
        if (deposit.availableAmount().compareTo(amount) < 0) {
            throw new BusinessException("DEPOSIT_INSUFFICIENT_FUNDS", "Insufficient available balance");
        }
        deposit.block(amount);
        return toDto(repository.save(deposit));
    }

    private DepositEntity find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("DEPOSIT_NOT_FOUND", "Deposit not found: " + id));
    }

    private static DepositDto toDto(DepositEntity e) {
        return new DepositDto(e.getId(), e.getPersonId(), e.getCurrencyCode(), e.getBalance(), e.getBlockedAmount());
    }
}
