package com.misc.shafagh.loan.impl.repository;

import com.misc.shafagh.loan.impl.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {
}
