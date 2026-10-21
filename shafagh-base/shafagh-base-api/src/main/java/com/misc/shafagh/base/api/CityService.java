package com.misc.shafagh.base.api;
import com.misc.shafagh.base.api.dto.CityDto;
import java.util.List;
public interface CityService {
    List<CityDto> findAll();
    List<CityDto> findAllIncludingProvinces();
    CityDto getById(Long id);
    CityDto save(CityDto dto);
    CityDto update(Long id, CityDto dto);
}
