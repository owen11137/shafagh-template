package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.common.SchemaName;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.Date;


@Entity
@Table(schema = SchemaName.CIF, name = "SIGNATURE")
@SequenceGenerator(name = "SEQ_SIGNATURE_ID", schema = SchemaName.CIF, sequenceName = "SEQ_SIGNATURE_ID", allocationSize = 1)
@Getter
@Setter
public class SignatureEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SIGNATURE_ID")
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
