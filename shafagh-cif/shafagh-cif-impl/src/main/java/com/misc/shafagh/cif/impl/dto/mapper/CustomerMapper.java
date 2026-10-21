package com.misc.shafagh.cif.impl.dto.mapper;



import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.base.api.dto.CityDto;
import com.misc.shafagh.cif.impl.dto.response.CustomerDto;
import com.misc.shafagh.cif.impl.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {CustomerAddressMapper.class, CustomerContactMapper.class, SignatureMapper.class, CityDtoResolver.class}, componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "signatures", ignore = true)
    CustomerEntity toEntity(CustomerReqDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "signatures", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    void updateEntityFromDto(CustomerReqDTO dto, @MappingTarget CustomerEntity entity);

    @Mapping(target = "city", source = "cityId")
    CustomerDto toDto(CustomerEntity entity);

    List<CustomerDto> toDtos(List<CustomerEntity> entities);

}
