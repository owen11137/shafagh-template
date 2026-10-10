package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.core.entity.enums.AddressType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CUSTOMER_ADDRESS")
@Getter
@Setter
@SequenceGenerator(name = "CUSTOMER_ADDRESS_ID_SEQ", sequenceName = "CUSTOMER_ADDRESS_ID_SEQ", allocationSize = 1)
public class CustomerAddressEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CUSTOMER_ADDRESS_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ADDRESS_TYPE")
    private AddressType addressType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false,foreignKey = @ForeignKey(name = "CUSTOMER_ADDRESS_FK_CITY"))
    private CityEntity city;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "CUSTOMER_ADDRESS_FK_CUSTOMER"))
    private CustomerEntity customer;
}
