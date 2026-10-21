package com.misc.shafagh.loan.impl.dto.mapper;

import com.misc.shafagh.loan.impl.dto.request.LoanReqDTO;
import com.misc.shafagh.loan.impl.dto.response.LoanResponseDto;
import com.misc.shafagh.loan.impl.entity.LoanEntity;
import com.misc.shafagh.loan.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت Loan؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface LoanMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    LoanEntity toEntity(LoanReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    void updateFromRequest(LoanReqDTO request, @MappingTarget LoanEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    LoanResponseDto toResponse(LoanEntity entity);

    java.util.List<LoanResponseDto> toResponses(java.util.List<LoanEntity> entities);
}
