package com.misc.shafagh.base.impl.dto.mapper;
import com.misc.shafagh.base.api.dto.BranchDto;
import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface BranchMapper {
    @Mapping(target="fundId", source="fund.id")
    BranchDto toDto(BranchEntity entity);
    @Mapping(target="fund", ignore=true)
    @Mapping(target="branchContactList", ignore=true)
    BranchEntity toEntity(BranchDto dto);
    @Mapping(target="id", ignore=true)
    @Mapping(target="fund", ignore=true)
    @Mapping(target="branchContactList", ignore=true)
    void update(BranchDto dto, @MappingTarget BranchEntity entity);
}
