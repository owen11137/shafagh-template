package com.misc.shafagh.base.impl.repository;


import com.misc.shafagh.base.impl.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {
    void deleteDistinctById(Long id);
    CityEntity findFirstByCode(String code);
}
