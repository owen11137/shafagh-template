package com.misc.shafagh.cif.impl.dto.mapper;


import com.misc.client.infra.starter.v3.dto.mapper.BaseMapper;

import com.misc.shafagh.core.dto.request.CustomerReqDTO;
import com.misc.shafagh.core.dto.response.CityDto;
import com.misc.shafagh.core.dto.response.CustomerDto;
import com.misc.shafagh.core.entity.CityEntity;
import com.misc.shafagh.core.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {BaseMapper.class, CustomerAddressMapper.class}, componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "city", source = "cityId", qualifiedByName = "cityFromId")
    @Mapping(target = "signatures", ignore = true)
    CustomerEntity toEntity(CustomerReqDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "city", source = "cityId", qualifiedByName = "cityFromId")
    @Mapping(target = "signatures", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    void updateEntityFromDto(CustomerReqDTO dto, @MappingTarget CustomerEntity entity);

    CustomerDto toDto(CustomerEntity entity);

    List<CustomerDto> toDtos(List<CustomerEntity> entities);

    CityDto toCityDto(CityEntity entity);

    @Named("cityFromId")
    default CityEntity cityFromId(Long cityId) {
        if (cityId == null) {
            return null;
        }
        CityEntity city = new CityEntity();
        city.setId(cityId);
        return city;
    }
}