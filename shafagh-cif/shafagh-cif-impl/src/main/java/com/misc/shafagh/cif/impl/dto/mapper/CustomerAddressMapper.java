package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.client.infra.starter.v3.dto.mapper.BaseMapper;
import com.misc.shafagh.cif.impl.dto.request.CustomerAddressReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerAddressDto;
import com.misc.shafagh.cif.impl.entity.CityEntity;
import com.misc.shafagh.cif.impl.entity.CustomerAddressEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(uses = BaseMapper.class, componentModel = "spring")
public interface CustomerAddressMapper {

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "city", ignore = true)   // ← city رو ignore کن
    CustomerAddressEntity toEntity(CustomerAddressReqDTO dto);

    List<CustomerAddressEntity> toEntities(List<CustomerAddressReqDTO> dtos);

    CustomerAddressDto toDto(CustomerAddressEntity entity);

    List<CustomerAddressDto> toDtos(List<CustomerAddressEntity> entities);

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "city", ignore = true)
    void updateEntityFromDto(CustomerAddressReqDTO dto, @MappingTarget CustomerAddressEntity entity);

    @AfterMapping
    default void fillCity(CustomerAddressReqDTO dto, @MappingTarget CustomerAddressEntity entity) {
        if (dto.getCityId() != null) {
            CityEntity city = new CityEntity();
            city.setId(dto.getCityId());
            entity.setCity(city);
        }
    }
}