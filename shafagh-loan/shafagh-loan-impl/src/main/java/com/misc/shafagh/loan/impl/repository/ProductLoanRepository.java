package com.misc.shafagh.loan.impl.repository;

import com.misc.shafagh.loan.impl.entity.ProductLoanEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductLoanRepository extends JpaRepository<ProductLoanEntity, Long> {
    Optional<ProductLoanEntity> findByIdAndFundId(Long id, Long fundId);
    Page<ProductLoanEntity> findAllByFundId(Long fundId, Pageable pageable);
    boolean existsByCodeAndFundId(String code, Long fundId);
    boolean existsByCodeAndFundIdAndIdNot(String code, Long fundId, Long id);
}
