package com.misc.shafagh.cif.impl.dto.mapper;

import com.misc.shafagh.cif.impl.dto.request.CustomerInquiryReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerInquiryResponseDto;
import com.misc.shafagh.cif.impl.entity.CustomerInquiryEntity;
import com.misc.shafagh.cif.impl.dto.mapper.EntityReferenceResolver;
import org.mapstruct.*;

/** نگاشت CustomerInquiry؛ ایجاد و ویرایش، شناسه/tenant/auditing را از ورودی دریافت نمی‌کنند. */
@Mapper(componentModel = "spring", uses = EntityReferenceResolver.class)
public interface CustomerInquiryMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "customer", source = "customerId", qualifiedByName = "entityReference")
    @Mapping(target = "customerContact", source = "customerContactId", qualifiedByName = "entityReference")
    CustomerInquiryEntity toEntity(CustomerInquiryReqDTO request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedOn", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fundId", ignore = true)
    @Mapping(target = "customer", source = "customerId", qualifiedByName = "entityReference")
    @Mapping(target = "customerContact", source = "customerContactId", qualifiedByName = "entityReference")
    void updateFromRequest(CustomerInquiryReqDTO request, @MappingTarget CustomerInquiryEntity entity);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.ERROR)
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerContactId", source = "customerContact.id")
    CustomerInquiryResponseDto toResponse(CustomerInquiryEntity entity);

    java.util.List<CustomerInquiryResponseDto> toResponses(java.util.List<CustomerInquiryEntity> entities);
}
