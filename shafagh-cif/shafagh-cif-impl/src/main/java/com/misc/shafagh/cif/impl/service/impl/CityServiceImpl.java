package com.misc.shafagh.cif.impl.service.impl;


import com.misc.shafagh.core.dto.mapper.CityMapper;
import com.misc.shafagh.core.dto.response.CityDto;
import com.misc.shafagh.core.entity.CityEntity;
import com.misc.shafagh.core.repository.CityRepository;
import com.misc.shafagh.core.service.api.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CityServiceImpl implements CityService {
    @Autowired
    CityRepository cityRepository;
    @Autowired
    CityMapper cityMapper;

    @Override
    public List<CityDto> findAll() {

        List<CityEntity> cities = cityRepository.findAll();

        // ساخت Map استان‌ها
        Map<Long, String> provinceMap = cities.stream()
                .filter(city -> city.getParentId() == 0)
                .collect(Collectors.toMap(
                        CityEntity::getId,
                        CityEntity::getName
                ));

        return cities.stream()
                // حذف استان‌ها
                .filter(city -> city.getParentId() != 0)
                .map(city -> {

                    CityDto dto = cityMapper.toDto(city);

                    // اضافه کردن نام استان
                    String provinceName = provinceMap.get(city.getParentId());
                    dto.setProvince(provinceName);

                    return dto;
                })
                .toList();
    }
}
