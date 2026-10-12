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
    @Transactional(transactionManager = "cifTransactionManager", readOnly=true)
    public boolean exists(Long id) { return id!=null && repository.existsById(id); }
}
