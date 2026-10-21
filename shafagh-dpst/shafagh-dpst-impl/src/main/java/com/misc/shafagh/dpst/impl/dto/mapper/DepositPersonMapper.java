package com.misc.shafagh.dpst.impl.dto.mapper;

import com.misc.shafagh.dpst.impl.dto.request.DepositPersonReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.DepositPersonResponseDto;
import com.misc.shafagh.dpst.impl.entity.DepositPersonEntity;
import com.misc.shafagh.dpst.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت DepositPerson؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface DepositPersonMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "deposit", source = "depositId", qualifiedByName = "entityReference")
    DepositPersonEntity toEntity(DepositPersonReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "deposit", source = "depositId", qualifiedByName = "entityReference")
    void updateFromRequest(DepositPersonReqDTO request, @MappingTarget DepositPersonEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "depositId", source = "deposit.id")
    DepositPersonResponseDto toResponse(DepositPersonEntity entity);

    java.util.List<DepositPersonResponseDto> toResponses(java.util.List<DepositPersonEntity> entities);
}
