package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

/** Exercises every new mapper as an injected Spring bean, including managed reference IDs. */
@SpringBootTest
@Transactional
class EntityDtoMapperTest {
    @Autowired private ApplicationContext context;
    record MapperCase(String mapper, String entity, String request, String response, boolean audited) {}

    static Stream<MapperCase> models() {
        return List.of(
        new MapperCase("com.misc.shafagh.base.impl.dto.mapper.CityMapper", "com.misc.shafagh.base.impl.entity.CityEntity", "com.misc.shafagh.base.impl.dto.request.CityReqDTO", "com.misc.shafagh.base.impl.dto.response.CityResponseDto", false),
        new MapperCase("com.misc.shafagh.base.impl.dto.mapper.branch.BranchAddressMapper", "com.misc.shafagh.base.impl.entity.branch.BranchAddressEntity", "com.misc.shafagh.base.impl.dto.request.branch.BranchAddressReqDTO", "com.misc.shafagh.base.impl.dto.response.branch.BranchAddressResponseDto", false),
        new MapperCase("com.misc.shafagh.base.impl.dto.mapper.branch.BranchContactMapper", "com.misc.shafagh.base.impl.entity.branch.BranchContactEntity", "com.misc.shafagh.base.impl.dto.request.branch.BranchContactReqDTO", "com.misc.shafagh.base.impl.dto.response.branch.BranchContactResponseDto", false),
        new MapperCase("com.misc.shafagh.base.impl.dto.mapper.BranchMapper", "com.misc.shafagh.base.impl.entity.branch.BranchEntity", "com.misc.shafagh.base.impl.dto.request.branch.BranchReqDTO", "com.misc.shafagh.base.impl.dto.response.branch.BranchResponseDto", false),
        new MapperCase("com.misc.shafagh.base.impl.dto.mapper.fund.FundAddressMapper", "com.misc.shafagh.base.impl.entity.fund.FundAddressEntity", "com.misc.shafagh.base.impl.dto.request.fund.FundAddressReqDTO", "com.misc.shafagh.base.impl.dto.response.fund.FundAddressResponseDto", false),
        new MapperCase("com.misc.shafagh.base.impl.dto.mapper.fund.FundContactMapper", "com.misc.shafagh.base.impl.entity.fund.FundContactEntity", "com.misc.shafagh.base.impl.dto.request.fund.FundContactReqDTO", "com.misc.shafagh.base.impl.dto.response.fund.FundContactResponseDto", false),
        new MapperCase("com.misc.shafagh.base.impl.dto.mapper.FundMapper", "com.misc.shafagh.base.impl.entity.fund.FundEntity", "com.misc.shafagh.base.impl.dto.request.fund.FundReqDTO", "com.misc.shafagh.base.impl.dto.response.fund.FundResponseDto", false),
        new MapperCase("com.misc.shafagh.cif.impl.dto.mapper.CustomerInquiryMapper", "com.misc.shafagh.cif.impl.entity.CustomerInquiryEntity", "com.misc.shafagh.cif.impl.dto.request.CustomerInquiryReqDTO", "com.misc.shafagh.cif.impl.dto.response.CustomerInquiryResponseDto", true),
        new MapperCase("com.misc.shafagh.dpst.impl.dto.mapper.DepositMapper", "com.misc.shafagh.dpst.impl.entity.DepositEntity", "com.misc.shafagh.dpst.impl.dto.request.DepositReqDTO", "com.misc.shafagh.dpst.impl.dto.response.DepositResponseDto", true),
        new MapperCase("com.misc.shafagh.dpst.impl.dto.mapper.DepositPersonMapper", "com.misc.shafagh.dpst.impl.entity.DepositPersonEntity", "com.misc.shafagh.dpst.impl.dto.request.DepositPersonReqDTO", "com.misc.shafagh.dpst.impl.dto.response.DepositPersonResponseDto", true),
        new MapperCase("com.misc.shafagh.dpst.impl.dto.mapper.ProductDepositMapper", "com.misc.shafagh.dpst.impl.entity.ProductDepositEntity", "com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO", "com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto", true),
        new MapperCase("com.misc.shafagh.loan.impl.dto.mapper.LoanMapper", "com.misc.shafagh.loan.impl.entity.LoanEntity", "com.misc.shafagh.loan.impl.dto.request.LoanReqDTO", "com.misc.shafagh.loan.impl.dto.response.LoanResponseDto", true),
        new MapperCase("com.misc.shafagh.loan.impl.dto.mapper.LoanPersonMapper", "com.misc.shafagh.loan.impl.entity.LoanPersonEntity", "com.misc.shafagh.loan.impl.dto.request.LoanPersonReqDTO", "com.misc.shafagh.loan.impl.dto.response.LoanPersonResponseDto", true),
        new MapperCase("com.misc.shafagh.loan.impl.dto.mapper.ProductLoanMapper", "com.misc.shafagh.loan.impl.entity.ProductLoanEntity", "com.misc.shafagh.loan.impl.dto.request.ProductLoanReqDTO", "com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto", true),
        new MapperCase("com.misc.shafagh.loan.impl.dto.mapper.installment.InstallmentDetailMapper", "com.misc.shafagh.loan.impl.entity.installment.InstallmentDetailEntity", "com.misc.shafagh.loan.impl.dto.request.installment.InstallmentDetailReqDTO", "com.misc.shafagh.loan.impl.dto.response.installment.InstallmentDetailResponseDto", true),
        new MapperCase("com.misc.shafagh.loan.impl.dto.mapper.installment.InstallmentMapper", "com.misc.shafagh.loan.impl.entity.installment.InstallmentEntity", "com.misc.shafagh.loan.impl.dto.request.installment.InstallmentReqDTO", "com.misc.shafagh.loan.impl.dto.response.installment.InstallmentResponseDto", true)
        ).stream();
    }

    @ParameterizedTest
    @MethodSource("models")
    void mapsAllRequestFieldsAndProtectsIdentityAndAuditFields(MapperCase model) throws Exception {
        Class<?> mapperType = Class.forName(model.mapper());
        Class<?> entityType = Class.forName(model.entity());
        Class<?> requestType = Class.forName(model.request());
        Class<?> responseType = Class.forName(model.response());
        Object mapper = context.getBean(mapperType);
        Object request = requestType.getConstructor().newInstance();
        var requestProperties = new BeanWrapperImpl(request);
        for (var field : requestType.getDeclaredFields()) {
            assertFalse(List.of("id", "createdBy", "createdOn", "modifiedBy", "modifiedOn", "version").contains(field.getName()));
            if (model.audited()) assertNotEquals("fundId", field.getName());
            requestProperties.setPropertyValue(field.getName(), sample(field.getType()));
        }
        Object entity = mapperType.getMethod("toEntity", requestType).invoke(mapper, request);
        var entityProperties = new BeanWrapperImpl(entity);
        assertNull(entityProperties.getPropertyValue("id"));
        entityProperties.setPropertyValue("id", 42L);
        if (model.audited()) {
            entityProperties.setPropertyValue("fundId", 37L);
            entityProperties.setPropertyValue("createdBy", "original");
            entityProperties.setPropertyValue("version", 5L);
        }
        Object response = mapperType.getMethod("toResponse", entityType).invoke(mapper, entity);
        assertEquals(responseType, response.getClass());
        var responseProperties = new BeanWrapperImpl(response);
        for (var field : requestType.getDeclaredFields()) {
            assertEquals(requestProperties.getPropertyValue(field.getName()), responseProperties.getPropertyValue(field.getName()), model.entity()+"."+field.getName());
        }
        mapperType.getMethod("updateFromRequest", requestType, entityType).invoke(mapper, request, entity);
        assertEquals(42L, entityProperties.getPropertyValue("id"));
        if (model.audited()) {
            assertEquals(37L, entityProperties.getPropertyValue("fundId"));
            assertEquals("original", entityProperties.getPropertyValue("createdBy"));
            assertEquals(5L, entityProperties.getPropertyValue("version"));
        }
        assertNull(mapperType.getMethod("toEntity", requestType).invoke(mapper, new Object[]{null}));
        assertNull(mapperType.getMethod("toResponse", entityType).invoke(mapper, new Object[]{null}));
        var list = (List<?>) mapperType.getMethod("toResponses", List.class).invoke(mapper, List.of(entity));
        assertEquals(1, list.size());
        assertEquals(42L, new BeanWrapperImpl(list.getFirst()).getPropertyValue("id"));
    }

    private Object sample(Class<?> type) {
        if (type == String.class) return "متن آزمایشی";
        if (type == Long.class || type == long.class) return 7L;
        if (type == Integer.class || type == int.class) return 3;
        if (type == Boolean.class || type == boolean.class) return true;
        if (type == BigDecimal.class) return new BigDecimal("123.45");
        if (type == Date.class) return new Date(1_700_000_000_000L);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 10, 12, 12, 30);
        if (type == byte[].class) return new byte[]{1, 2, 3};
        if (type.isEnum()) return type.getEnumConstants()[0];
        throw new IllegalArgumentException("No test sample for " + type.getName());
    }
}
