package com.misc.shafagh.cif.impl.service.api;

import com.misc.shafagh.core.dto.response.CityDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CityService {
    List<CityDto> findAll();
}
