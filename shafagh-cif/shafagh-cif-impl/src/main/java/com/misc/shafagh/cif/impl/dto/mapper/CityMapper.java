package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.client.infra.starter.v3.dto.mapper.BaseMapper;
import com.misc.shafagh.core.dto.response.CityDto;
import com.misc.shafagh.core.entity.CityEntity;
import com.misc.shafagh.core.entity.CustomerAddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = BaseMapper.class, componentModel = "spring")
public interface CityMapper {
    CityEntity toEntity(CityDto dto);

    List<CustomerAddressEntity> toEntities(List<CityDto> dtos);

    CityDto toDto(CityEntity entity);

    List<CityDto> toDtos(List<CityDto> entities);

    void updateEntityFromDto(CityDto dto, @MappingTarget CityEntity entity);

}