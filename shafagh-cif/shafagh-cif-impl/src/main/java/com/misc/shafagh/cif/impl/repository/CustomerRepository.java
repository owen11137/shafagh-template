package com.misc.shafagh.cif.impl.repository;


import com.misc.shafagh.cif.impl.entity.CustomerEntity;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository

    public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {
        Page<CustomerEntity> findAllByCustomerTypeIn(Collection<CustomerType> customerTypes, Pageable pageable);
        CustomerEntity getFirstByIdAndCustomerTypeIn(long id, Collection<CustomerType> customerTypes);
        CustomerEntity getFirstByIdentifier(String identifier);

    Optional<CustomerEntity> findByIdentifierAndCustomerTypeAndCustomerStatus(String identifier,
        CustomerType type, com.misc.shafagh.cif.impl.entity.enums.CustomerStatus status);
    Optional<CustomerEntity> findByIdAndFundId(Long id, Long fundId);

    }

