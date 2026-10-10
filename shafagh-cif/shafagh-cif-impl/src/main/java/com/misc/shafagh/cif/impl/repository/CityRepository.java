package com.misc.shafagh.cif.impl.repository;


import com.misc.shafagh.cif.impl.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {
    void deleteDistinctById(Long id);
    CityEntity findFirstByCode(String code);
}
