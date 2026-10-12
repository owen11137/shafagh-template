package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.cif.impl.entity.enums.CustomerInquiryStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Table(schema = CifSchema.NAME, name = "CUSTOMER_INQUIRY")
@Entity(name = "CUSTOMERInquiry")
@SequenceGenerator(name = "CUSTOMER_INQUIRY_ID_SEQ", schema = CifSchema.NAME, sequenceName = "CUSTOMER_INQUIRY_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class CustomerInquiryEntity extends TenantAwareEntity {
    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CUSTOMER_INQUIRY_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CUSTOMER_TYPE", nullable = false)
    private CustomerType customerType;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "CUSTOMER_INQUIRY_FK_CUSTOMER"), nullable = false)
    private CustomerEntity customer;

    @Column(name = "RESPONSE")
    private String response;

    @Column(name = "RESPONSE_VALUE")
    private int responseValue;

    @Column(name = "RESPONSE_HEADER")
    private String responseHeader;

    @Column(name = "STATUS")
    @Enumerated(EnumType.ORDINAL)
    private CustomerInquiryStatus customerInquiryStatus;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_CONTACT_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "CUSTOMER_INQUIRY_FK_CUSTOMER_CONTACT"))
    private CustomerContactEntity customerContact;
}


