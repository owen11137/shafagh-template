package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.common.SchemaName;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = SchemaName.CIF, name = "CUSTOMER_CONTACT")
@SequenceGenerator(name = "CUSTOMER_CONTACT_ID_SEQ", schema = SchemaName.CIF, sequenceName = "CUSTOMER_CONTACT_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class CustomerContactEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CUSTOMER_CONTACT_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;
    

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "CUSTOMER_CONTACT_FK_CUSTOMER"), nullable = false)
    private CustomerEntity customer;
}
