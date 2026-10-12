package com.misc.shafagh.base.impl.dto.mapper;
import com.misc.shafagh.base.impl.dto.request.fund.FundReqDTO;
import com.misc.shafagh.base.impl.dto.response.fund.FundResponseDto;
import com.misc.shafagh.base.api.dto.FundDto;
import com.misc.shafagh.base.impl.entity.fund.FundEntity;
import org.mapstruct.*;
@Mapper(componentModel="spring", uses=EntityReferenceResolver.class)
public interface FundMapper {
    FundDto toDto(FundEntity entity);
    @Mapping(target="branches", ignore=true)
    FundEntity toEntity(FundDto dto);
    @Mapping(target="id", ignore=true)
    @Mapping(target="branches", ignore=true)
    void update(FundDto dto, @MappingTarget FundEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branches", ignore = true)
    FundEntity toEntity(FundReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branches", ignore = true)
    void updateFromRequest(FundReqDTO request, @MappingTarget FundEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    FundResponseDto toResponse(FundEntity entity);

    java.util.List<FundResponseDto> toResponses(java.util.List<FundEntity> entities);
}
