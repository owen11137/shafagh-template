package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.cif.impl.entity.enums.AddressType;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = SchemaName.CIF, name = "CUSTOMER_ADDRESS", comment = "جدول نشانی‌های مشتری")
@Getter
@Setter
@SequenceGenerator(name = "SEQ_CUSTOMER_ADDRESS_ID", schema = SchemaName.CIF, sequenceName = "SEQ_CUSTOMER_ADDRESS_ID", allocationSize = 1)
public class CustomerAddressEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای رکورد نشانی‌های مشتری")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CUSTOMER_ADDRESS_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ADDRESS_TYPE", comment = "نوع نشانی مشتری")
    private AddressType addressType;

    @Column(name = "CITY_ID", nullable = false, comment = "شناسه شهر مرتبط با رکورد")
    private Long cityId;

    @Column(name = "ADDRESS", comment = "متن کامل نشانی مشتری")
    private String address;

    @Column(name = "POSTAL_CODE", comment = "کد پستی نشانی")
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CUSTOMER_ADDRESS_CUSTOMER"), comment = "شناسه مشتری مرتبط با رکورد")
    private CustomerEntity customer;
}
