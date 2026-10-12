package com.misc.shafagh.base.impl.dto.mapper;

import com.misc.shafagh.base.api.dto.CityDto;
import com.misc.shafagh.base.impl.entity.CityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CityMapper {
    CityEntity toEntity(CityDto dto);
    @Mapping(target = "province", ignore = true)
    CityDto toDto(CityEntity entity);
    List<CityDto> toDtos(List<CityEntity> entities);
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CityDto dto, @MappingTarget CityEntity entity);
}
