package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.core.entity.TenantAwareEntity;
import com.misc.shafagh.core.entity.party.CustomerEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DEPOSIT_PERSON")
@Comment("جدول اشخاص مرتبط با سپرده")
@SequenceGenerator(name = "DEPOSIT_PERSON_ID_SEQ", sequenceName = "DEPOSIT_PERSON_ID_SEQ", allocationSize = 1)
@Data
public class DepositPersonEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPOSIT_PERSON_ID_SEQ")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای شخص سپرده")
    private Long id;

    @Column(name = "DEPOSIT_PERSON_TYPE")
    @Comment("نقش شخص در سپرده (صاحب حساب, صاحب امضاء)")
    @Enumerated(EnumType.ORDINAL)
    private DepositPersonType depositPersonType;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPOSIT_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "deposit_depositPerson_fk"))
    @Comment("شناسه سپرده")
    private DepositEntity deposit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "deposit_customer_fk"))
    @Comment("شناسه مشتری")
    private CustomerEntity customer;

    @Column(name = "DESCRIPTION")
    @Comment("توضیحات")
    private String description;

    @Column(name = "FROM_DATE")
    @Comment("تاریخ شروع")
    private LocalDateTime fromDate;

    @Column(name = "TO_DATE")
    @Comment("تاریخ پایان")
    private LocalDateTime toDate;
}