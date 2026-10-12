package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.shafagh.cif.impl.dto.request.CustomerAddressReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerAddressDto;
import com.misc.shafagh.cif.impl.entity.CustomerAddressEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = CityDtoResolver.class)
public interface CustomerAddressMapper {

    @Mapping(target = "customer", ignore = true)
    CustomerAddressEntity toEntity(CustomerAddressReqDTO dto);

    List<CustomerAddressEntity> toEntities(List<CustomerAddressReqDTO> dtos);

    @Mapping(target = "city", source = "cityId")
    CustomerAddressDto toDto(CustomerAddressEntity entity);

    List<CustomerAddressDto> toDtos(List<CustomerAddressEntity> entities);

    @Mapping(target = "customer", ignore = true)
    void updateEntityFromDto(CustomerAddressReqDTO dto, @MappingTarget CustomerAddressEntity entity);

}
