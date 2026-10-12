package com.misc.shafagh.loan.impl.dto.mapper;

import com.misc.shafagh.loan.impl.dto.request.ProductLoanReqDTO;
import com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto;
import com.misc.shafagh.loan.impl.entity.ProductLoanEntity;
import com.misc.shafagh.loan.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت ProductLoan؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface ProductLoanMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    ProductLoanEntity toEntity(ProductLoanReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    void updateFromRequest(ProductLoanReqDTO request, @MappingTarget ProductLoanEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    ProductLoanResponseDto toResponse(ProductLoanEntity entity);

    java.util.List<ProductLoanResponseDto> toResponses(java.util.List<ProductLoanEntity> entities);
}
