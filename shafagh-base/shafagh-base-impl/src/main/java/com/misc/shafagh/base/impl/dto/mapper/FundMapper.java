package com.misc.shafagh.base.impl.dto.mapper;
import com.misc.shafagh.base.api.dto.FundDto;
import com.misc.shafagh.base.impl.entity.fund.FundEntity;
import org.mapstruct.*;
@Mapper(componentModel="spring")
public interface FundMapper {
    FundDto toDto(FundEntity entity);
    @Mapping(target="branches", ignore=true)
    FundEntity toEntity(FundDto dto);
    @Mapping(target="id", ignore=true)
    @Mapping(target="branches", ignore=true)
    void update(FundDto dto, @MappingTarget FundEntity entity);
}
