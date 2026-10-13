package com.misc.shafagh;
import static org.junit.jupiter.api.Assertions.*;
import com.misc.shafagh.base.api.BranchService;
import com.misc.shafagh.base.api.dto.BranchDto;
import com.misc.shafagh.base.impl.entity.CityEntity;
import com.misc.shafagh.base.impl.entity.branch.*;
import com.misc.shafagh.base.impl.entity.fund.FundEntity;
import jakarta.persistence.EntityManagerFactory;
import java.util.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(properties="shafagh.fund-id=57")
class BranchTenantIsolationTest {
    @Autowired EntityManagerFactory factory;
    @Autowired BranchService branches;
    @Test void scopesBranchesAddressesAndContactsAndRejectsCrossFundLinks() {
        var sessions=factory.unwrap(SessionFactory.class);
        Map<Long,Long> ids=new HashMap<>();Map<Long,Long> addressIds=new HashMap<>();Map<Long,Long> contactIds=new HashMap<>();
        Long cityId;
        try(var session=sessions.openSession()) {
            var tx=session.beginTransaction();
            for(long fund:List.of(57L,58L)) if(session.find(FundEntity.class,fund)==null)
                session.createNativeMutationQuery("insert into SHFQ_BASE.FUND(ID,CODE,NAME,OWNER_ID) values(:id,:code,'tenant test',:id)")
                    .setParameter("id",fund).setParameter("code","T-"+fund).executeUpdate();
            CityEntity city=new CityEntity();city.setName("tenant test city");city.setParentId(0L);session.persist(city);cityId=city.getId();tx.commit();
        }
        BranchEntity foreign;
        for(long fund:List.of(57L,58L)) {
            try(var session=sessions.withOptions().tenantIdentifier(fund).openSession()) {
                var tx=session.beginTransaction();
                BranchEntity branch=new BranchEntity();branch.setName("Branch "+fund);branch.setCode("SAME-CODE");
                branch.setFund(session.getReference(FundEntity.class,fund));session.persist(branch);session.flush();
                assertEquals(fund,branch.getFundId());ids.put(fund,branch.getId());
                BranchAddressEntity address=new BranchAddressEntity();address.setBranchEntity(branch);
                address.setCity(session.getReference(CityEntity.class,cityId));session.persist(address);
                BranchContactEntity contact=new BranchContactEntity();contact.setBranch(branch);contact.setContactNumber("02112345678");session.persist(contact);
                session.flush();assertEquals(fund,address.getFundId());assertEquals(fund,contact.getFundId());
                assertNotNull(address.getCreatedOn());assertNotNull(contact.getCreatedOn());
                addressIds.put(fund,address.getId());contactIds.put(fund,contact.getId());tx.commit();
            }
        }
        try(var other=sessions.withOptions().tenantIdentifier(58L).openSession()) { foreign=other.find(BranchEntity.class,ids.get(58L)); }
        try(var session=sessions.openSession()) {
            assertNotNull(session.find(BranchEntity.class,ids.get(57L)));
            assertNull(session.find(BranchEntity.class,ids.get(58L)));
            assertNull(session.find(BranchAddressEntity.class,addressIds.get(58L)));
            assertNull(session.find(BranchContactEntity.class,contactIds.get(58L)));
            assertTrue(session.createQuery("from BRANCH where id=:id",BranchEntity.class).setParameter("id",ids.get(58L)).getResultList().isEmpty());
        }
        assertEquals(404,assertThrows(ResponseStatusException.class,()->branches.getById(ids.get(58L))).getStatusCode().value());
        BranchDto request=new BranchDto();request.setCode("FOREIGN");request.setName("Foreign");request.setFundId(58L);
        assertEquals(403,assertThrows(ResponseStatusException.class,()->branches.save(request)).getStatusCode().value());
        for(boolean address:List.of(true,false)) {
            try(var session=sessions.openSession()) {
                var tx=session.beginTransaction();
                assertThrows(RuntimeException.class,()->{
                    if(address) {
                        var wrong=new BranchAddressEntity();wrong.setFundId(57L);wrong.setBranchEntity(foreign);wrong.setCity(session.getReference(CityEntity.class,cityId));session.persist(wrong);
                    } else {
                        var wrong=new BranchContactEntity();wrong.setFundId(57L);wrong.setBranch(foreign);session.persist(wrong);
                    }
                    session.flush();
                });tx.rollback();
            }
        }
        try(var session=sessions.openSession()) {
            var tx=session.beginTransaction();
            var contact=session.find(BranchContactEntity.class,contactIds.get(57L));contact.setBranch(foreign);
            assertThrows(RuntimeException.class,session::flush);tx.rollback();
        }
        request.setFundId(null);request.setCode("AUTO-TENANT");
        assertEquals(57L,branches.save(request).getFundId());
    }
}
