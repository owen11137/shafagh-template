package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.client.infra.starter.v3.dto.mapper.BaseMapper;
import com.misc.shafagh.core.dto.response.SignatureDto;
import com.misc.shafagh.core.entity.SignatureEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * فقط جهت خواندن (Entity -> DTO). ساخت و ویرایش امضا در CustomerSignatureHelper انجام می‌شود.
 */
@Mapper(uses = BaseMapper.class, componentModel = "spring")
public interface SignatureMapper {

    @Mapping(target = "customerId", source = "customer.id")
    SignatureDto toDto(SignatureEntity entity);

    List<SignatureDto> toDtos(List<SignatureEntity> entities);
}
