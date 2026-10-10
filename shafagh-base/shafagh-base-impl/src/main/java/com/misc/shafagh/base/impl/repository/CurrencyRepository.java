package com.misc.shafagh.base.impl.repository;

import com.misc.shafagh.base.impl.entity.CurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<CurrencyEntity, String> {
}
