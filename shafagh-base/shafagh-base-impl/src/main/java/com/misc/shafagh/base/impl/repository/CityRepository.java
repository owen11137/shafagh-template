package com.misc.shafagh.base.impl.repository;


import com.misc.shafagh.base.impl.entity.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Long> {
    @org.springframework.data.jpa.repository.Query("select c from CityEntity c where c.parentId <> 0 or c.parentId is null")
    org.springframework.data.domain.Page<CityEntity> findCities(org.springframework.data.domain.Pageable pageable);
    void deleteDistinctById(Long id);
    CityEntity findFirstByCode(String code);
}
