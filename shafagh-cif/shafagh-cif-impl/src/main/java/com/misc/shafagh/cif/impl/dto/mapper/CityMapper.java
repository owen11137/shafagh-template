package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.shafagh.cif.impl.dto.response.CityDto;
import com.misc.shafagh.cif.impl.entity.CityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CityMapper {
    CityEntity toEntity(CityDto dto);
    CityDto toDto(CityEntity entity);
    List<CityDto> toDtos(List<CityEntity> entities);
    void updateEntityFromDto(CityDto dto, @MappingTarget CityEntity entity);
}
