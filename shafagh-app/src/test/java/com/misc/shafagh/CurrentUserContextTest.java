package com.misc.shafagh;
import static org.junit.jupiter.api.Assertions.*;
import com.misc.shafagh.common.context.CurrentUserContext;
import com.misc.shafagh.common.context.UserContext;
import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.entity.DepositType;
import com.misc.shafagh.dpst.impl.service.api.ProductDepositService;
import com.misc.shafagh.dpst.impl.exception.ProductDepositNotFoundException;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties="shafagh.fund-id=999")
@Import(CurrentUserContextTest.ContextConfiguration.class)
class CurrentUserContextTest {
    @org.springframework.test.context.DynamicPropertySource
    static void database(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:user_context;MODE=Oracle;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SHFQ_BASE\\;CREATE SCHEMA IF NOT EXISTS SHFQ_CIF\\;CREATE SCHEMA IF NOT EXISTS SHFQ_DPST\\;CREATE SCHEMA IF NOT EXISTS SHFQ_LOAN");
    }
    static final ThreadLocal<UserContext> identity=ThreadLocal.withInitial(()->new UserContext(63,17,23));
    @Autowired CurrentUserContext context;
    @Autowired org.springframework.context.ApplicationContext applicationContext;
    @Autowired ProductDepositService products;
    @TestConfiguration(proxyBeanMethods=false)
    static class ContextConfiguration {
        @Bean CurrentUserContext testIdentity() { return identity::get; }
    }
    ProductDepositReqDTO request() {
        var dto=new ProductDepositReqDTO();dto.setCode(UUID.randomUUID().toString());
        dto.setDepositType(DepositType.DEPOSIT1);dto.setStatus(ProductStatus.ACTIVE);return dto;
    }
    @Test void replacingTheProviderUpdatesServiceTenantAndAuditingWithoutRestart() {
        try {
            assertEquals(1,applicationContext.getBeansOfType(CurrentUserContext.class).size());
            assertEquals(new UserContext(63,17,23),context.current());
            var first=products.create(request());assertEquals(63L,first.getFundId());assertEquals("17",first.getCreatedBy());
            identity.set(new UserContext(64,18,24));
            assertThrows(ProductDepositNotFoundException.class,()->products.getById(first.getId()));
            var second=products.create(request());assertEquals(64L,second.getFundId());assertEquals("18",second.getCreatedBy());
            assertEquals(24L,context.branchId());
            identity.set(new UserContext(63,19,25));
            var update=request();update.setCode(first.getCode());update.setTitle("updated by another user");
            var updated=products.update(first.getId(),update);
            assertEquals("17",updated.getCreatedBy());assertEquals("19",updated.getModifiedBy());
            assertThrows(ProductDepositNotFoundException.class,()->products.getById(second.getId()));
        } finally { identity.remove(); }
    }
    @Test void parallelOperationsKeepEachCallerFundAndUser() throws Exception {
        try(var executor=Executors.newFixedThreadPool(2)) {
            var jobs=new ArrayList<Callable<Boolean>>();
            for(long fund:List.of(71L,72L)) jobs.add(()->{
                try {
                    identity.set(new UserContext(fund,fund+100,fund+200));
                    var product=products.create(request());
                    return product.getFundId()==fund && product.getCreatedBy().equals(Long.toString(fund+100))
                        && context.branchId()==fund+200 && products.getById(product.getId()).getFundId()==fund;
                } finally { identity.remove(); }
            });
            for(var result:executor.invokeAll(jobs)) assertTrue(result.get());
        }
    }
    @Test void rejectsIncompleteOrInvalidIdentity() {
        assertThrows(IllegalArgumentException.class,()->new UserContext(0,1,1));
        assertThrows(IllegalArgumentException.class,()->new UserContext(1,-1,1));
        assertThrows(IllegalArgumentException.class,()->new UserContext(1,1,0));
    }
}
