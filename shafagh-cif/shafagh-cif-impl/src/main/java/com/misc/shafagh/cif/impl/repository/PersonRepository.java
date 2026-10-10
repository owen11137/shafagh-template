package com.misc.shafagh.cif.impl.repository;

import com.misc.shafagh.cif.impl.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    boolean existsByNationalCode(String nationalCode);
}
