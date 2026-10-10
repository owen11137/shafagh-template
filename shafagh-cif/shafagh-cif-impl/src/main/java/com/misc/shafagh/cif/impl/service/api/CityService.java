package com.misc.shafagh.cif.impl.service.api;

import com.misc.shafagh.cif.impl.dto.response.CityDto;

import java.util.List;

public interface CityService {
    List<CityDto> findAll();
}
