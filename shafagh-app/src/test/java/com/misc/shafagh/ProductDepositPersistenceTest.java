package com.misc.shafagh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.misc.shafagh.base.api.dto.ProductStatus;
import com.misc.shafagh.dpst.api.dto.DepositType;
import com.misc.shafagh.dpst.impl.DpstSchema;
import com.misc.shafagh.dpst.impl.entity.ProductDepositEntity;
import com.misc.shafagh.dpst.impl.repository.ProductDepositRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.sql.DataSource;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Real committed transactions, usable on H2 and the Oracle test profile. */
@SpringBootTest
class ProductDepositPersistenceTest {

    private static final String TABLE = DpstSchema.NAME + ".PRODUCT_DEPOSIT";

    @Autowired private ProductDepositRepository repository;
    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private DataSource dataSource;
    @Value("${shafagh.fund-id:1}") private long fundId;

    private final List<Long> insertedIds = new ArrayList<>();
    private JdbcTemplate jdbc;
    private SessionFactory sessionFactory;

    @BeforeEach
    void preparePersistenceAccess() {
        jdbc = new JdbcTemplate(dataSource);
        sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    }

    @AfterEach
    void deleteOnlyTestProducts() {
        for (Long id : insertedIds) {
            jdbc.update("DELETE FROM " + TABLE + " WHERE ID = ?", id);
        }
        insertedIds.clear();
    }

    @Test
    void productRoundTripsWithNationalizedTextOrdinalEnumsAndValidityDates() {
        ProductDepositEntity product = newProduct();
        String code = product.getCode();
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 10, 20, 30);
        LocalDateTime to = from.plusYears(1);
        product.setFromDate(from);
        product.setToDate(to);

        Long id = insert(product).getId();
        ProductDepositEntity reloaded = repository.findById(id).orElseThrow();

        assertEquals("سپرده سرمایه‌گذاری", reloaded.getTitle());
        assertEquals(code, reloaded.getCode());
        assertEquals("محصول سپرده برای مشتریان فارسی‌زبان", reloaded.getDescription());
        assertEquals(ProductStatus.INACTIVE, reloaded.getStatus());
        assertEquals(DepositType.DEPOSIT2, reloaded.getDepositType());
        assertEquals(from, reloaded.getFromDate());
        assertEquals(to, reloaded.getToDate());
        assertEquals(fundId, reloaded.getFundId());
        assertEquals(ProductStatus.INACTIVE.ordinal(), jdbc.queryForObject(
                "SELECT STATUS FROM " + TABLE + " WHERE ID = ?", Integer.class, id));
        assertEquals(DepositType.DEPOSIT2.ordinal(), jdbc.queryForObject(
                "SELECT DEPOSIT_TYPE FROM " + TABLE + " WHERE ID = ?", Integer.class, id));
        assertEquals(from, jdbc.queryForObject(
                "SELECT FROM_DATE FROM " + TABLE + " WHERE ID = ?", Timestamp.class, id).toLocalDateTime());
        assertEquals(to, jdbc.queryForObject(
                "SELECT TO_DATE FROM " + TABLE + " WHERE ID = ?", Timestamp.class, id).toLocalDateTime());
    }

    @Test
    void auditingPopulatesDatesAndUpdatingPreservesCreationWhileIncrementingVersion() {
        ProductDepositEntity product = insert(newProduct());
        Date createdOn = product.getCreatedOn();
        Long initialVersion = product.getVersion();
        assertNotNull(createdOn);
        assertNotNull(product.getModifiedOn());
        assertEquals(0L, initialVersion);

        // A sentinel verifies the auditing listener replaces the value without clock sleeps.
        product.setModifiedOn(new Date(0));
        product.setTitle("عنوان ویرایش‌شده");
        repository.saveAndFlush(product);

        ProductDepositEntity updated = repository.findById(product.getId()).orElseThrow();
        assertEquals("عنوان ویرایش‌شده", updated.getTitle());
        assertEquals(createdOn.getTime(), updated.getCreatedOn().getTime());
        assertTrue(updated.getModifiedOn().after(new Date(0)));
        assertFalse(updated.getModifiedOn().before(createdOn));
        assertEquals(initialVersion + 1, updated.getVersion());
        assertEquals(updated.getVersion(), jdbc.queryForObject(
                "SELECT OPT_LOCK FROM " + TABLE + " WHERE ID = ?", Long.class, product.getId()));
    }

    @Test
    void tenantsCannotLoadQueryOrUpdateEachOthersProductsOrSpoofOwnership() {
        long otherFundId = fundId == 1 ? 2 : 1;
        ProductDepositEntity ownProduct = insert(newProduct());
        ProductDepositEntity otherProduct = inTenant(otherFundId, session -> {
            ProductDepositEntity product = newProduct();
            session.persist(product);
            insertedIds.add(product.getId());
            return product;
        });
        assertEquals(otherFundId, otherProduct.getFundId());
        List<Long> ids = List.of(ownProduct.getId(), otherProduct.getId());

        assertTrue(repository.findById(otherProduct.getId()).isEmpty());
        assertEquals(List.of(ownProduct.getId()), repository.findAllById(ids).stream()
                .map(ProductDepositEntity::getId).toList());

        inTenant(otherFundId, session -> {
            assertNull(session.find(ProductDepositEntity.class, ownProduct.getId()));
            assertEquals(List.of(otherProduct.getId()), session.createQuery(
                    "select p.id from ProductDepositEntity p where p.id in :ids", Long.class)
                    .setParameter("ids", ids).getResultList());
            int changed = session.createMutationQuery(
                    "update ProductDepositEntity p set p.title = :title where p.id = :id")
                    .setParameter("title", "unauthorized change")
                    .setParameter("id", ownProduct.getId()).executeUpdate();
            assertEquals(0, changed);
            return null;
        });
        assertEquals(ownProduct.getTitle(), repository.findById(ownProduct.getId()).orElseThrow().getTitle());

        ProductDepositEntity spoofed = newProduct();
        spoofed.setFundId(fundId);
        RollbackException error = assertThrows(RollbackException.class, () ->
                inTenant(otherFundId, session -> {
                    session.persist(spoofed);
                    insertedIds.add(spoofed.getId());
                    return null;
                }));
        PropertyValueException cause = assertInstanceOf(PropertyValueException.class, error.getCause());
        assertEquals("fundId", cause.getPropertyName());
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + TABLE + " WHERE CODE = ?", Integer.class, spoofed.getCode()));
    }

    @Test
    void staleUpdateCannotOverwriteAnAlreadyCommittedProductChange() {
        ProductDepositEntity product = insert(newProduct());

        try (Session first = sessionFactory.withOptions().tenantIdentifier(fundId).openSession();
             Session stale = sessionFactory.withOptions().tenantIdentifier(fundId).openSession()) {
            Transaction firstTransaction = first.beginTransaction();
            Transaction staleTransaction = stale.beginTransaction();
            try {
                ProductDepositEntity firstCopy = first.find(ProductDepositEntity.class, product.getId());
                ProductDepositEntity staleCopy = stale.find(ProductDepositEntity.class, product.getId());
                firstCopy.setTitle("committed change");
                firstTransaction.commit();

                staleCopy.setTitle("stale change");
                assertThrows(OptimisticLockException.class, stale::flush);
            } finally {
                if (firstTransaction.isActive()) {
                    firstTransaction.rollback();
                }
                if (staleTransaction.isActive()) {
                    staleTransaction.rollback();
                }
            }
        }

        ProductDepositEntity persisted = repository.findById(product.getId()).orElseThrow();
        assertEquals("committed change", persisted.getTitle());
        assertEquals(product.getVersion() + 1, persisted.getVersion());
    }

    private ProductDepositEntity newProduct() {
        ProductDepositEntity product = new ProductDepositEntity();
        product.setTitle("سپرده سرمایه‌گذاری");
        product.setCode("سپرده-" + UUID.randomUUID());
        product.setDescription("محصول سپرده برای مشتریان فارسی‌زبان");
        product.setStatus(ProductStatus.INACTIVE);
        product.setDepositType(DepositType.DEPOSIT2);
        return product;
    }

    private ProductDepositEntity insert(ProductDepositEntity product) {
        ProductDepositEntity saved = repository.saveAndFlush(product);
        insertedIds.add(saved.getId());
        return saved;
    }

    private <T> T inTenant(long tenantId, Function<Session, T> action) {
        try (Session session = sessionFactory.withOptions().tenantIdentifier(tenantId).openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                T result = action.apply(session);
                transaction.commit();
                return result;
            } finally {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            }
        }
    }
}
