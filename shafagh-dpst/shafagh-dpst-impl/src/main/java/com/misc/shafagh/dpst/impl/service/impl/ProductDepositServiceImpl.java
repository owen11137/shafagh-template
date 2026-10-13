package com.misc.shafagh.dpst.impl.service.impl;

import com.misc.shafagh.dpst.impl.dto.mapper.ProductDepositMapper;
import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto;
import com.misc.shafagh.dpst.impl.entity.ProductDepositEntity;
import com.misc.shafagh.dpst.impl.exception.*;
import com.misc.shafagh.dpst.impl.repository.ProductDepositRepository;
import com.misc.shafagh.dpst.impl.service.port.ProductDepositService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional("dpstTransactionManager")
public class ProductDepositServiceImpl implements ProductDepositService {
    private final ProductDepositRepository repository;
    private final com.misc.shafagh.dpst.impl.repository.DepositRepository deposits;
    private final ProductDepositMapper mapper;
    private final Validator validator;
    private final long fundId;

    public ProductDepositServiceImpl(ProductDepositRepository repository, ProductDepositMapper mapper,
            Validator validator, com.misc.shafagh.dpst.impl.repository.DepositRepository deposits, @Value("${shafagh.fund-id:1}") long fundId) {
        this.deposits = deposits; this.repository = repository; this.mapper = mapper; this.validator = validator; this.fundId = fundId;
    }

    public ProductDepositResponseDto create(ProductDepositReqDTO request) {
        validate(request);
        if (repository.existsByCodeAndFundId(request.getCode(), fundId)) throw new DuplicateProductDepositCodeException();
        return persist(mapper.toEntity(request));
    }

    @Transactional(transactionManager = "dpstTransactionManager", readOnly = true)
    public ProductDepositResponseDto getById(Long id) { return mapper.toResponse(require(id)); }

    @Transactional(transactionManager = "dpstTransactionManager", readOnly = true)
    public Page<ProductDepositResponseDto> findAll(Pageable pageable) {
        return repository.findAllByFundId(fundId, pageable).map(mapper::toResponse);
    }

    public ProductDepositResponseDto update(Long id, ProductDepositReqDTO request) {
        ProductDepositEntity entity = require(id, true);
        validate(request);
        if (repository.existsByCodeAndFundIdAndIdNot(request.getCode(), fundId, id)) throw new DuplicateProductDepositCodeException();
        if (!entity.getCode().equals(request.getCode()) && deposits.existsByProductCodeAndFundId(entity.getCode(), fundId))
            throw new ProductDepositInUseException();
        mapper.updateFromRequest(request, entity);
        return persist(entity);
    }

    public void delete(Long id) {
        ProductDepositEntity entity = require(id, true);
        if (deposits.existsByProductCodeAndFundId(entity.getCode(), fundId)) throw new ProductDepositInUseException();
        repository.delete(entity); repository.flush();
    }

    private ProductDepositEntity require(Long id) { return require(id, false); }

    private ProductDepositEntity require(Long id, boolean lock) {
        if (id == null || id <= 0) throw new InvalidProductDepositException("شناسه محصول باید مثبت باشد");
        return (lock ? repository.findLockedByIdAndFundId(id, fundId) : repository.findByIdAndFundId(id, fundId)).orElseThrow(ProductDepositNotFoundException::new);
    }

    private ProductDepositResponseDto persist(ProductDepositEntity entity) {
        try {
            // Flush before mapping so generated tenant/audit/version fields are returned.
            return mapper.toResponse(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof org.hibernate.exception.ConstraintViolationException violation
                        && violation.getConstraintName() != null
                        && (violation.getConstraintName().toUpperCase(java.util.Locale.ROOT).contains("UK_PRODUCT_DEPOSIT_CODE")
                        || violation.getConstraintName().toUpperCase(java.util.Locale.ROOT).contains("UDX_PRODUCT_DEPOSIT_CODE"))) {
                    throw new DuplicateProductDepositCodeException();
                }
            }
            throw exception;
        }
    }

    private void validate(ProductDepositReqDTO request) {
        if (request == null) throw new InvalidProductDepositException("اطلاعات محصول الزامی است");
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new InvalidProductDepositException(violations.stream()
                .map(ConstraintViolation::getMessage).sorted().collect(Collectors.joining(" | ")));
        request.setCode(request.getCode().trim());
        if (request.getFromDate() != null && request.getToDate() != null && request.getToDate().isBefore(request.getFromDate())) {
            throw new InvalidProductDepositException("تاریخ پایان نباید قبل از تاریخ شروع باشد");
        }
    }

}
