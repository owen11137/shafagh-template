package com.misc.shafagh.loan.impl.dto.mapper.installment;

import com.misc.shafagh.loan.impl.dto.request.installment.InstallmentDetailReqDTO;
import com.misc.shafagh.loan.impl.dto.response.installment.InstallmentDetailResponseDto;
import com.misc.shafagh.loan.impl.entity.installment.InstallmentDetailEntity;
import com.misc.shafagh.loan.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت InstallmentDetail؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface InstallmentDetailMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "loan", source = "loanId", qualifiedByName = "entityReference")
    InstallmentDetailEntity toEntity(InstallmentDetailReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "loan", source = "loanId", qualifiedByName = "entityReference")
    void updateFromRequest(InstallmentDetailReqDTO request, @MappingTarget InstallmentDetailEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "loanId", source = "loan.id")
    InstallmentDetailResponseDto toResponse(InstallmentDetailEntity entity);

    java.util.List<InstallmentDetailResponseDto> toResponses(java.util.List<InstallmentDetailEntity> entities);
}
