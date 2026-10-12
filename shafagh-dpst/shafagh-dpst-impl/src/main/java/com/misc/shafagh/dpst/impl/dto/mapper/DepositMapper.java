package com.misc.shafagh.dpst.impl.dto.mapper;

import com.misc.shafagh.dpst.impl.dto.request.DepositReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.DepositResponseDto;
import com.misc.shafagh.dpst.impl.entity.DepositEntity;
import com.misc.shafagh.dpst.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت Deposit؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface DepositMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    DepositEntity toEntity(DepositReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    void updateFromRequest(DepositReqDTO request, @MappingTarget DepositEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    DepositResponseDto toResponse(DepositEntity entity);

    java.util.List<DepositResponseDto> toResponses(java.util.List<DepositEntity> entities);
}
