package com.misc.shafagh.loan.impl.dto.mapper;

import com.misc.shafagh.loan.impl.dto.request.LoanPersonReqDTO;
import com.misc.shafagh.loan.impl.dto.response.LoanPersonResponseDto;
import com.misc.shafagh.loan.impl.entity.LoanPersonEntity;
import com.misc.shafagh.loan.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت LoanPerson؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface LoanPersonMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "loan", source = "loanId", qualifiedByName = "entityReference")
    LoanPersonEntity toEntity(LoanPersonReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "loan", source = "loanId", qualifiedByName = "entityReference")
    void updateFromRequest(LoanPersonReqDTO request, @MappingTarget LoanPersonEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "loanId", source = "loan.id")
    LoanPersonResponseDto toResponse(LoanPersonEntity entity);

    java.util.List<LoanPersonResponseDto> toResponses(java.util.List<LoanPersonEntity> entities);
}
