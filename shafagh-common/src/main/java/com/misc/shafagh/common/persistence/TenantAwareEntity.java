package com.misc.shafagh.common.persistence;

import com.misc.infra.framework4.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
@AttributeOverrides({
    @AttributeOverride(name="createdBy", column=@Column(name="CREATED_BY", nullable=true, updatable=false, length=30, comment="شناسه کاربر ایجادکننده رکورد")),
    @AttributeOverride(name="createdOn", column=@Column(name="CREATED_ON", nullable=true, updatable=false, comment="زمان ایجاد رکورد")),
    @AttributeOverride(name="modifiedBy", column=@Column(name="MODIFIED_BY", nullable=true, length=30, comment="شناسه آخرین کاربر ویرایش‌کننده رکورد")),
    @AttributeOverride(name="modifiedOn", column=@Column(name="MODIFIED_ON", nullable=true, comment="زمان آخرین ویرایش رکورد")),
    @AttributeOverride(name="version", column=@Column(name="OPT_LOCK", nullable=false, columnDefinition="number(19,0) default 0", comment="نسخه رکورد برای کنترل هم‌زمانی و قفل خوش‌بینانه"))
})
@Getter
@Setter
public abstract class TenantAwareEntity extends BaseEntity {

    @TenantId
    @Column(name = "FUND_ID", nullable = false, updatable = false, comment = "شناسه صندوق (مستاجر) مالک رکورد")
    private Long fundId;
}
