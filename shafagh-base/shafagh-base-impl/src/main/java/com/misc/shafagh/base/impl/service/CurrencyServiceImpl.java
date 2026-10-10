package com.misc.shafagh.base.impl.service;

import com.misc.shafagh.base.api.BusinessException;
import com.misc.shafagh.base.api.CurrencyService;
import com.misc.shafagh.base.api.dto.CurrencyDto;
import com.misc.shafagh.base.impl.entity.CurrencyEntity;
import com.misc.shafagh.base.impl.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository repository;

    CurrencyServiceImpl(CurrencyRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CurrencyDto create(CurrencyDto currency) {
        if (repository.existsById(currency.code())) {
            throw new BusinessException("CURRENCY_DUPLICATE", "Currency already exists: " + currency.code());
        }
        return toDto(repository.save(new CurrencyEntity(currency.code(), currency.name())));
    }

    @Override
    public CurrencyDto getByCode(String code) {
        return repository.findById(code)
                .map(CurrencyServiceImpl::toDto)
                .orElseThrow(() -> new BusinessException("CURRENCY_NOT_FOUND", "Currency not found: " + code));
    }

    @Override
    public boolean exists(String code) {
        return repository.existsById(code);
    }

    private static CurrencyDto toDto(CurrencyEntity e) {
        return new CurrencyDto(e.getCode(), e.getName());
    }
}
