package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.client.infra.starter.v3.dto.mapper.BaseMapper;
import com.misc.shafagh.core.dto.request.CustomerAddressReqDTO;
import com.misc.shafagh.core.dto.response.CustomerAddressDto;
import com.misc.shafagh.core.entity.CityEntity;
import com.misc.shafagh.core.entity.CustomerAddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = BaseMapper.class, componentModel = "spring")
public interface CustomerContactMapper {
    @Mapping(target = "customer", ignore = true)
    CustomerAddressEntity toEntity(CustomerAddressReqDTO dto);

    List<CustomerAddressEntity> toEntities(List<CustomerAddressReqDTO> dtos);

    CustomerAddressDto toDto(CustomerAddressEntity entity);

    List<CustomerAddressDto> toDtos(List<CustomerAddressEntity> entities);

    @Mapping(target = "customer", ignore = true)
    void updateEntityFromDto(CustomerAddressReqDTO dto, @MappingTarget CustomerAddressEntity entity);

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