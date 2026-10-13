package com.misc.shafagh.cif.impl.entity;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.common.SchemaName;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Comment("جدول اطلاعات تماس مشتری")
@Entity
@Table(schema = SchemaName.CIF, name = "CUSTOMER_CONTACT")
@SequenceGenerator(name = "SEQ_CUSTOMER_CONTACT_ID", schema = SchemaName.CIF, sequenceName = "SEQ_CUSTOMER_CONTACT_ID", allocationSize = 1)
@Getter
@Setter
public class CustomerContactEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CUSTOMER_CONTACT_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد اطلاعات تماس مشتری")
    private Long id;
    

    @Column(name = "PHONE_NUMBER")
    @Comment("شماره تلفن مشتری")
    private String phoneNumber;

    @Column(name = "MOBILE_NUMBER")
    @Comment("شماره تلفن همراه مشتری")
    private String mobileNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_CUSTOMER_CONTACT_CUSTOMER"), nullable = false)
    @Comment("شناسه مشتری مرتبط با رکورد")
    private CustomerEntity customer;
}
