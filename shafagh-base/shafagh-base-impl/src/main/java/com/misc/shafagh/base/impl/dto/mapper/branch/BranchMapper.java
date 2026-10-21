package com.misc.shafagh.base.impl.dto.mapper.branch;
import com.misc.shafagh.base.impl.dto.mapper.EntityReferenceResolver;
import com.misc.shafagh.base.impl.dto.request.branch.BranchReqDTO;
import com.misc.shafagh.base.impl.dto.response.branch.BranchResponseDto;
import com.misc.shafagh.base.api.dto.BranchDto;
import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import org.mapstruct.*;
@Mapper(componentModel="spring", uses= EntityReferenceResolver.class)
public interface BranchMapper {
    @Mapping(target="fundId", source="fund.id")
    BranchDto toDto(BranchEntity entity);
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target="fund", ignore=true)
    @Mapping(target="branchContactList", ignore=true)
    BranchEntity toEntity(BranchDto dto);
    @Mapping(target="id", ignore=true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target="fund", ignore=true)
    @Mapping(target="branchContactList", ignore=true)
    void update(BranchDto dto, @MappingTarget BranchEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchContactList", ignore = true)
    @Mapping(target = "fund", source = "fundId", qualifiedByName = "entityReference")
    BranchEntity toEntity(BranchReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "branchContactList", ignore = true)
    @Mapping(target = "fund", source = "fundId", qualifiedByName = "entityReference")
    void updateFromRequest(BranchReqDTO request, @MappingTarget BranchEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "fundId", source = "fund.id")
    BranchResponseDto toResponse(BranchEntity entity);

    java.util.List<BranchResponseDto> toResponses(java.util.List<BranchEntity> entities);
}
