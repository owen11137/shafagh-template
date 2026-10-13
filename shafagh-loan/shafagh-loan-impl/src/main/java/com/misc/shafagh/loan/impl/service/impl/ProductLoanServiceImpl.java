package com.misc.shafagh.loan.impl.service.impl;

import com.misc.shafagh.loan.impl.dto.mapper.ProductLoanMapper;
import com.misc.shafagh.loan.impl.dto.request.ProductLoanReqDTO;
import com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto;
import com.misc.shafagh.loan.impl.entity.ProductLoanEntity;
import com.misc.shafagh.loan.impl.exception.*;
import com.misc.shafagh.loan.impl.repository.ProductLoanRepository;
import com.misc.shafagh.loan.impl.service.api.ProductLoanService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import com.misc.shafagh.common.context.CurrentUserContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("loanTransactionManager")
public class ProductLoanServiceImpl implements ProductLoanService {
    private final ProductLoanRepository repository;
    private final ProductLoanMapper mapper;
    private final Validator validator;
    private final CurrentUserContext currentUserContext;

    public ProductLoanServiceImpl(ProductLoanRepository repository, ProductLoanMapper mapper,
            Validator validator, CurrentUserContext currentUserContext) {
        this.repository = repository; this.mapper = mapper; this.validator = validator; this.currentUserContext = currentUserContext;
    }

    public ProductLoanResponseDto create(ProductLoanReqDTO request) {
        validate(request);
        if (repository.existsByCodeAndFundId(request.getCode(), currentUserContext.fundId())) throw new DuplicateProductLoanCodeException();
        return persist(mapper.toEntity(request));
    }

    @Transactional(transactionManager = "loanTransactionManager", readOnly = true)
    public ProductLoanResponseDto getById(Long id) { return mapper.toResponse(require(id)); }

    @Transactional(transactionManager = "loanTransactionManager", readOnly = true)
    public Page<ProductLoanResponseDto> findAll(Pageable pageable) {
        return repository.findAllByFundId(currentUserContext.fundId(), pageable).map(mapper::toResponse);
    }

    public ProductLoanResponseDto update(Long id, ProductLoanReqDTO request) {
        ProductLoanEntity entity = require(id);
        validate(request);
        if (repository.existsByCodeAndFundIdAndIdNot(request.getCode(), currentUserContext.fundId(), id)) throw new DuplicateProductLoanCodeException();
        mapper.updateFromRequest(request, entity);
        return persist(entity);
    }

    public void delete(Long id) { repository.delete(require(id)); repository.flush(); }

    private ProductLoanEntity require(Long id) {
        if (id == null || id <= 0) throw new InvalidProductLoanException("شناسه محصول باید مثبت باشد");
        return repository.findByIdAndFundId(id, currentUserContext.fundId()).orElseThrow(ProductLoanNotFoundException::new);
    }

    private ProductLoanResponseDto persist(ProductLoanEntity entity) {
        try {
            // Flush before mapping so generated tenant/audit/version fields are returned.
            return mapper.toResponse(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException violation
                        && violation.getConstraintName() != null
                        && (violation.getConstraintName().toUpperCase(java.util.Locale.ROOT).contains("UK_PRODUCT_LOAN_CODE")
                        || violation.getConstraintName().toUpperCase(java.util.Locale.ROOT).contains("UDX_PRODUCT_LOAN_CODE"))) {
                    throw new DuplicateProductLoanCodeException();
                }
            }
            throw exception;
        }
    }

    private void validate(ProductLoanReqDTO request) {
        if (request == null) throw new InvalidProductLoanException("اطلاعات محصول الزامی است");
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new InvalidProductLoanException(violations.stream()
                .map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining(" | ")));
        request.setCode(request.getCode().trim());
        range(request.getMinAmount(), request.getMaxAmount(), "مبلغ");
        range(request.getMinFee(), request.getMaxFee(), "کارمزد");
        range(request.getMinFine(), request.getMaxFine(), "جریمه");
        if (request.getMinInstallmentCount() != null && request.getMaxInstallmentCount() != null
                && request.getMinInstallmentCount() > request.getMaxInstallmentCount()) {
            throw new InvalidProductLoanException("حداقل تعداد اقساط نباید از حداکثر بیشتر باشد");
        }
        if (request.getFromDate() != null && request.getToDate() != null && request.getToDate().isBefore(request.getFromDate())) {
            throw new InvalidProductLoanException("تاریخ پایان نباید قبل از تاریخ شروع باشد");
        }
    }

    private void range(BigDecimal min, BigDecimal max, String title) {
        if (min != null && max != null && min.compareTo(max) > 0)
            throw new InvalidProductLoanException("حداقل " + title + " نباید از حداکثر بیشتر باشد");
    }
}
