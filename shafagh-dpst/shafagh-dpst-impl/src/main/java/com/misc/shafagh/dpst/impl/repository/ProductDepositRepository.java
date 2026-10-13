package com.misc.shafagh.dpst.impl.repository;
import com.misc.shafagh.dpst.impl.entity.ProductDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductDepositRepository extends JpaRepository<ProductDepositEntity, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<ProductDepositEntity> findByCodeAndFundId(String code, Long fundId);
    java.util.Optional<ProductDepositEntity> findByIdAndFundId(Long id, Long fundId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<ProductDepositEntity> findLockedByIdAndFundId(Long id, Long fundId);
    boolean existsByCodeAndFundId(String code, Long fundId);
    boolean existsByCodeAndFundIdAndIdNot(String code, Long fundId, Long id);
    org.springframework.data.domain.Page<ProductDepositEntity> findAllByFundId(Long fundId, org.springframework.data.domain.Pageable pageable);
}
