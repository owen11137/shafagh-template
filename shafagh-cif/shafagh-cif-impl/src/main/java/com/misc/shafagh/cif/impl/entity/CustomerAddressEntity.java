package com.misc.shafagh.cif.impl.entity;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.cif.impl.entity.enums.AddressType;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Comment("جدول نشانی‌های مشتری")
@Entity
@Table(schema = SchemaName.CIF, name = "CUSTOMER_ADDRESS")
@Getter
@Setter
@SequenceGenerator(name = "SEQ_CUSTOMER_ADDRESS_ID", schema = SchemaName.CIF, sequenceName = "SEQ_CUSTOMER_ADDRESS_ID", allocationSize = 1)
public class CustomerAddressEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CUSTOMER_ADDRESS_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد نشانی‌های مشتری")
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ADDRESS_TYPE")
    @Comment("نوع نشانی مشتری")
    private AddressType addressType;

    @Column(name = "CITY_ID", nullable = false)
    @Comment("شناسه شهر مرتبط با رکورد")
    private Long cityId;

    @Column(name = "ADDRESS")
    @Comment("متن کامل نشانی مشتری")
    private String address;

    @Column(name = "POSTAL_CODE")
    @Comment("کد پستی نشانی")
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CUSTOMER_ADDRESS_CUSTOMER"))
    @Comment("شناسه مشتری مرتبط با رکورد")
    private CustomerEntity customer;
}
