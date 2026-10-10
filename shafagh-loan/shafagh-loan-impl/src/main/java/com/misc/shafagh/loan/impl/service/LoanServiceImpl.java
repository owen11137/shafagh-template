package com.misc.shafagh.loan.impl.service;

import com.misc.shafagh.base.api.BusinessException;
import com.misc.shafagh.cif.api.PersonService;
import com.misc.shafagh.dpst.api.DepositService;
import com.misc.shafagh.dpst.api.dto.DepositDto;
import com.misc.shafagh.loan.api.LoanService;
import com.misc.shafagh.loan.api.dto.LoanDto;
import com.misc.shafagh.loan.api.dto.RegisterLoanRequest;
import com.misc.shafagh.loan.impl.entity.LoanEntity;
import com.misc.shafagh.loan.impl.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class LoanServiceImpl implements LoanService {

    private final LoanRepository repository;
    private final PersonService personService;
    private final DepositService depositService;

    LoanServiceImpl(LoanRepository repository, PersonService personService, DepositService depositService) {
        this.repository = repository;
        this.personService = personService;
        this.depositService = depositService;
    }

    @Override
    @Transactional
    public LoanDto register(RegisterLoanRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new BusinessException("LOAN_INVALID_AMOUNT", "Loan amount must be positive");
        }
        if (!personService.existsActive(request.personId())) {
            throw new BusinessException("PERSON_NOT_ACTIVE", "Person is not active: " + request.personId());
        }
        DepositDto deposit = depositService.getById(request.depositId());
        if (!deposit.personId().equals(request.personId())) {
            throw new BusinessException("LOAN_DEPOSIT_OWNER_MISMATCH", "Deposit does not belong to the person");
        }
        return toDto(repository.save(new LoanEntity(request.personId(), request.depositId(), request.amount())));
    }

    @Override
    public LoanDto getById(Long id) {
        return repository.findById(id)
                .map(LoanServiceImpl::toDto)
                .orElseThrow(() -> new BusinessException("LOAN_NOT_FOUND", "Loan not found: " + id));
    }

    private static LoanDto toDto(LoanEntity e) {
        return new LoanDto(e.getId(), e.getPersonId(), e.getDepositId(), e.getAmount(), e.getStatus());
    }
}
