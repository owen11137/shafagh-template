package com.misc.shafagh.base.impl.dto.mapper.fund;

import com.misc.shafagh.base.impl.dto.request.fund.FundContactReqDTO;
import com.misc.shafagh.base.impl.dto.response.fund.FundContactResponseDto;
import com.misc.shafagh.base.impl.entity.fund.FundContactEntity;
import com.misc.shafagh.base.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت FundContact؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface FundContactMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundEntity", source = "fundEntityId", qualifiedByName = "entityReference")
    @Mapping(target = "branch", source = "branchId", qualifiedByName = "entityReference")
    FundContactEntity toEntity(FundContactReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundEntity", source = "fundEntityId", qualifiedByName = "entityReference")
    @Mapping(target = "branch", source = "branchId", qualifiedByName = "entityReference")
    void updateFromRequest(FundContactReqDTO request, @MappingTarget FundContactEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "fundEntityId", source = "fundEntity.id")
    @Mapping(target = "branchId", source = "branch.id")
    FundContactResponseDto toResponse(FundContactEntity entity);

    java.util.List<FundContactResponseDto> toResponses(java.util.List<FundContactEntity> entities);
}
