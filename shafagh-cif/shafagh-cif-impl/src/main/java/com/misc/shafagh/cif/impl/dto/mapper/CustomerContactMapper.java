package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.shafagh.cif.impl.dto.request.CustomerContactReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerContactDto;
import com.misc.shafagh.cif.impl.entity.CustomerContactEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerContactMapper {
    @Mapping(target = "customer", ignore = true)
    CustomerContactEntity toEntity(CustomerContactReqDTO dto);

    List<CustomerContactEntity> toEntities(List<CustomerContactReqDTO> dtos);

    CustomerContactDto toDto(CustomerContactEntity entity);

    List<CustomerContactDto> toDtos(List<CustomerContactEntity> entities);

    @Mapping(target = "customer", ignore = true)
    void updateEntityFromDto(CustomerContactReqDTO dto, @MappingTarget CustomerContactEntity entity);

}
