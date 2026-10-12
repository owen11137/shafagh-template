package com.misc.shafagh.dpst.impl.dto.mapper;

import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto;
import com.misc.shafagh.dpst.impl.entity.ProductDepositEntity;
import com.misc.shafagh.dpst.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت ProductDeposit؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface ProductDepositMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    ProductDepositEntity toEntity(ProductDepositReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    void updateFromRequest(ProductDepositReqDTO request, @MappingTarget ProductDepositEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    ProductDepositResponseDto toResponse(ProductDepositEntity entity);

    java.util.List<ProductDepositResponseDto> toResponses(java.util.List<ProductDepositEntity> entities);
}
