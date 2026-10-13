package com.misc.shafagh.base.impl.repository;
import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BranchRepository extends JpaRepository<BranchEntity,Long> {
    java.util.Optional<BranchEntity> findByIdAndFundId(Long id, Long fundId);
}
