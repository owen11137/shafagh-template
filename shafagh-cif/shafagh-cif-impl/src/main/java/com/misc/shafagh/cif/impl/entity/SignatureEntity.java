package com.misc.shafagh.cif.impl.entity;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.common.SchemaName;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.util.Date;


@Comment("جدول امضاهای مشتری")
@Entity
@Table(schema = SchemaName.CIF, name = "SIGNATURE")
@SequenceGenerator(name = "SEQ_SIGNATURE_ID", schema = SchemaName.CIF, sequenceName = "SEQ_SIGNATURE_ID", allocationSize = 1)
@Getter
@Setter
public class SignatureEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SIGNATURE_ID")
    @Comment("شناسه یکتای رکورد امضاهای مشتری")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "FROM_DATE")
    @Comment("تاریخ شروع اعتبار رکورد")
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "TO_DATE")
    @Comment("تاریخ پایان اعتبار رکورد")
    private Date toDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID",
            foreignKey = @ForeignKey(name = "FK_SIGNATURE_CUSTOMER"))
    @Comment("شناسه مشتری مرتبط با رکورد")
    private CustomerEntity customer;

    @Column(name = "IMAGE", length = 1000)
    @Comment("تصویر امضای مشتری")
    private byte[] image;

    @Nationalized
    @Column(name = "DESCRIPTION", length = 300)
    @Comment("توضیحات تکمیلی رکورد")
    private String description;

}
