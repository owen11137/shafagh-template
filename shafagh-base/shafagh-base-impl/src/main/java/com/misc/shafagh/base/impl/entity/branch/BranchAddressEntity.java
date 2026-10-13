package com.misc.shafagh.base.impl.entity.branch;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.base.impl.entity.CityEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = SchemaName.BASE, name = "BRANCH_ADDRESS", uniqueConstraints = @UniqueConstraint(name = "UK_BRANCH_ADDRESS_BRANCH_ID", columnNames = "BRANCH_ID"), comment = "جدول نشانی شعبه")
@SequenceGenerator(name = "SEQ_BRANCH_ADDRESS_ID", schema = SchemaName.BASE, sequenceName = "SEQ_BRANCH_ADDRESS_ID", allocationSize = 1)
@Getter
@Setter
public class BranchAddressEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای رکورد نشانی شعبه")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BRANCH_ADDRESS_ID")
    @EqualsAndHashCode.Include
    private Long id;

    // One address per owner is enforced by the explicitly named table unique constraint.
    @ManyToOne
    @JoinColumn(name = "BRANCH_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_BRANCH_ADDRESS_BRANCH"), comment = "شناسه شعبه مرتبط با رکورد")
    private BranchEntity branchEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_BRANCH_ADDRESS_CITY"), comment = "شناسه شهر مرتبط با رکورد")
    private CityEntity city;

    @Column(name = "MAIN_STREET", comment = "نام خیابان اصلی نشانی")
    private String mainStreet;

    @Column(name = "STREET", comment = "نام خیابان نشانی")
    private String street;

    @Column(name = "ALLEY", comment = "نام کوچه نشانی")
    private String alley;

    @Column(name = "TAG", comment = "پلاک نشانی")
    private String tag;

    @Column(name = "POSTAL_CODE", comment = "کد پستی نشانی")
    private String postalCode;

    @Column(name = "UNIT", comment = "شماره واحد نشانی")
    private String unit;
    @PrePersist
    @PreUpdate
    private void validateBranchTenant() {
        if (branchEntity == null) throw new IllegalArgumentException("Branch is required");
        Long branchFundId = branchEntity.getFundId();
        if (branchFundId == null) throw new IllegalArgumentException("Branch fund is required");
        if (getFundId() == null) setFundId(branchFundId);
        else if (!getFundId().equals(branchFundId)) throw new IllegalArgumentException("Branch belongs to another fund");
    }
}
