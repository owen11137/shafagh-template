package com.misc.shafagh.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * Base class for every entity that belongs to a single banking institution / legal
 * entity (tenant) in a multi-entity core banking deployment (e.g. a banking group
 * that operates several licensed entities or a shared platform serving several
 * banks).
 * <p>
 * {@code fundId} is stamped by Hibernate's discriminator-based multi-tenancy
 * from the current {@code CurrentTenantIdentifierResolver} on insert. Hibernate
 * entity queries are scoped to that tenant while sharing the same physical schema.
 * Native SQL and JDBC access must supply their own tenant restrictions.
 * <p>
 * Pure reference/master data that is intentionally shared across every institution
 * (e.g. {@code CountryEntity}, {@code CityEntity}) should extend {@link BaseEntity}
 * directly instead, bypassing tenant filtering.
 *
 * @author core-banking-party-model
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantAwareEntity extends BaseEntity {

    @TenantId
    @Column(name = "FUND_ID", comment = "شناسه صندوق (مستاجر) مالک رکورد")
    private Long fundId;
}
