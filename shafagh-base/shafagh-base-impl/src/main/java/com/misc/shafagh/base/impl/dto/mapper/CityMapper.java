package com.misc.shafagh.base.impl.dto.mapper;

import com.misc.shafagh.base.impl.dto.request.CityReqDTO;
import com.misc.shafagh.base.impl.dto.response.CityResponseDto;


import com.misc.shafagh.base.api.dto.CityDto;
import com.misc.shafagh.base.impl.entity.CityEntity;
import org.mapstruct.*;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface CityMapper {
    CityEntity toEntity(CityDto dto);
    @Mapping(target = "province", ignore = true)
    CityDto toDto(CityEntity entity);
    List<CityDto> toDtos(List<CityEntity> entities);
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CityDto dto, @MappingTarget CityEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    CityEntity toEntity(CityReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    void updateFromRequest(CityReqDTO request, @MappingTarget CityEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    CityResponseDto toResponse(CityEntity entity);

    java.util.List<CityResponseDto> toResponses(java.util.List<CityEntity> entities);
}
