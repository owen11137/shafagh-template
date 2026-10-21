package com.misc.shafagh.cif.impl.dto.mapper;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.base.api.dto.CityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class CityDtoResolver {
    private final CityService cityService;
    public CityDto resolve(Long id) { return id == null ? null : cityService.getById(id); }
}
