package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DEPOSIT_PERSON", schema = SchemaName.DP, comment = "جدول اشخاص مرتبط با سپرده")
@SequenceGenerator(name = "SEQ_DEPOSIT_PERSON_ID", schema = SchemaName.DP, sequenceName = "SEQ_DEPOSIT_PERSON_ID", allocationSize = 1)
@Data
public class DepositPersonEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای شخص سپرده")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DEPOSIT_PERSON_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "DEPOSIT_PERSON_TYPE", comment = "نقش شخص در سپرده (صاحب حساب, صاحب امضاء)")
    @Enumerated(EnumType.ORDINAL)
    private DepositPersonType depositPersonType;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPOSIT_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_DEPOSIT_PERSON_DEPOSIT"), comment = "شناسه سپرده")
    private DepositEntity deposit;

    @Column(name = "CUSTOMER_ID", comment = "شناسه مشتری")
    private long customerId;

    @Column(name = "DESCRIPTION", comment = "توضیحات")
    private String description;

    @Column(name = "FROM_DATE", comment = "تاریخ شروع")
    private LocalDateTime fromDate;

    @Column(name = "TO_DATE", comment = "تاریخ پایان")
    private LocalDateTime toDate;
}
