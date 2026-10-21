package com.misc.shafagh.base.impl.dto.mapper.branch;

import com.misc.shafagh.base.impl.dto.request.branch.BranchContactReqDTO;
import com.misc.shafagh.base.impl.dto.response.branch.BranchContactResponseDto;
import com.misc.shafagh.base.impl.entity.branch.BranchContactEntity;
import com.misc.shafagh.base.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت BranchContact؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface BranchContactMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branch", source = "branchId", qualifiedByName = "entityReference")
    BranchContactEntity toEntity(BranchContactReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branch", source = "branchId", qualifiedByName = "entityReference")
    void updateFromRequest(BranchContactReqDTO request, @MappingTarget BranchContactEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "branchId", source = "branch.id")
    BranchContactResponseDto toResponse(BranchContactEntity entity);

    java.util.List<BranchContactResponseDto> toResponses(java.util.List<BranchContactEntity> entities);
}
