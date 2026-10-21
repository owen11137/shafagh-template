package com.misc.shafagh.loan.impl.dto.mapper.installment;

import com.misc.shafagh.loan.impl.dto.request.installment.InstallmentReqDTO;
import com.misc.shafagh.loan.impl.dto.response.installment.InstallmentResponseDto;
import com.misc.shafagh.loan.impl.entity.installment.InstallmentEntity;
import com.misc.shafagh.loan.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت Installment؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface InstallmentMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "loan", source = "loanId", qualifiedByName = "entityReference")
    InstallmentEntity toEntity(InstallmentReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "loan", source = "loanId", qualifiedByName = "entityReference")
    void updateFromRequest(InstallmentReqDTO request, @MappingTarget InstallmentEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "loanId", source = "loan.id")
    InstallmentResponseDto toResponse(InstallmentEntity entity);

    java.util.List<InstallmentResponseDto> toResponses(java.util.List<InstallmentEntity> entities);
}
