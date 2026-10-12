package com.misc.shafagh.base.impl.dto.mapper.branch;

import com.misc.shafagh.base.impl.dto.request.branch.BranchAddressReqDTO;
import com.misc.shafagh.base.impl.dto.response.branch.BranchAddressResponseDto;
import com.misc.shafagh.base.impl.entity.branch.BranchAddressEntity;
import com.misc.shafagh.base.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت BranchAddress؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface BranchAddressMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branchEntity", source = "branchEntityId", qualifiedByName = "entityReference")
    @Mapping(target = "city", source = "cityId", qualifiedByName = "entityReference")
    BranchAddressEntity toEntity(BranchAddressReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branchEntity", source = "branchEntityId", qualifiedByName = "entityReference")
    @Mapping(target = "city", source = "cityId", qualifiedByName = "entityReference")
    void updateFromRequest(BranchAddressReqDTO request, @MappingTarget BranchAddressEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "branchEntityId", source = "branchEntity.id")
    @Mapping(target = "cityId", source = "city.id")
    BranchAddressResponseDto toResponse(BranchAddressEntity entity);

    java.util.List<BranchAddressResponseDto> toResponses(java.util.List<BranchAddressEntity> entities);
}
