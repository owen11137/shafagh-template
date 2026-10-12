package com.misc.shafagh.cif.impl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.Date;


@Entity
@Table(schema = CifSchema.NAME, name = "SIGNATURE")
@SequenceGenerator(name = "SIGNATURE_ID_SEQ", schema = CifSchema.NAME, sequenceName = "SIGNATURE_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class SignatureEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SIGNATURE_ID_SEQ")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "FROM_DATE")
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "TO_DATE")
    private Date toDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID",
            foreignKey = @ForeignKey(name = "FK_SIGNATURE_CUSTOMER"))
    private CustomerEntity customer;

    @Column(name = "IMAGE", length = 1000)
    private byte[] image;

    @Nationalized
    @Column(name = "DESCRIPTION", length = 300)
    private String description;

}
