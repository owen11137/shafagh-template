package com.misc.shafagh.base.impl.dto.mapper.fund;

import com.misc.shafagh.base.impl.dto.request.fund.FundAddressReqDTO;
import com.misc.shafagh.base.impl.dto.response.fund.FundAddressResponseDto;
import com.misc.shafagh.base.impl.entity.fund.FundAddressEntity;
import com.misc.shafagh.base.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت FundAddress؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface FundAddressMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundEntity", source = "fundEntityId", qualifiedByName = "entityReference")
    @Mapping(target = "city", source = "cityId", qualifiedByName = "entityReference")
    FundAddressEntity toEntity(FundAddressReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundEntity", source = "fundEntityId", qualifiedByName = "entityReference")
    @Mapping(target = "city", source = "cityId", qualifiedByName = "entityReference")
    void updateFromRequest(FundAddressReqDTO request, @MappingTarget FundAddressEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "fundEntityId", source = "fundEntity.id")
    @Mapping(target = "cityId", source = "city.id")
    FundAddressResponseDto toResponse(FundAddressEntity entity);

    java.util.List<FundAddressResponseDto> toResponses(java.util.List<FundAddressEntity> entities);
}
