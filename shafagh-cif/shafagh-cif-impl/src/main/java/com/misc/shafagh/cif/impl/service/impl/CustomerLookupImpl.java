package com.misc.shafagh.cif.impl.service.impl;
import com.misc.shafagh.cif.api.CustomerLookup;
import com.misc.shafagh.cif.impl.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class CustomerLookupImpl implements CustomerLookup {
    private final CustomerRepository repository;
    private final org.hibernate.context.spi.CurrentTenantIdentifierResolver<Long> tenantIdentifierResolver;
    @Transactional(transactionManager = "cifTransactionManager", readOnly=true)
    public boolean exists(Long id) { return id!=null && repository.existsById(id); }
    @Override
    @Transactional(transactionManager = "cifTransactionManager", readOnly=true)
    public boolean existsByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) return false;
        Long fundId = tenantIdentifierResolver.resolveCurrentTenantIdentifier();
        if (fundId == null || fundId <= 0) throw new IllegalStateException("Current fund tenant is required");
        return repository.existsByIdentifierAndFundId(identifier, fundId);
    }
    @Transactional(transactionManager = "cifTransactionManager", readOnly=true)
    public java.util.Optional<Long> findActiveIndividualId(String nationalCode) {
        return repository.findByIdentifierAndCustomerTypeAndCustomerStatus(nationalCode,
                com.misc.shafagh.cif.impl.entity.enums.CustomerType.REAL,
                com.misc.shafagh.cif.impl.entity.enums.CustomerStatus.ACTIVE).map(customer -> customer.getId());
    }
}
