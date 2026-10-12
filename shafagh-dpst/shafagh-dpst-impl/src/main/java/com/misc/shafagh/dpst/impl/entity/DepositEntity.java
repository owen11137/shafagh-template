package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "Deposit")
@Table(name = "DEPOSIT", schema = SchemaName.DPST)
@Comment("جدول سپرده‌ها")
@SequenceGenerator(name = "DEPOSIT_ID_SEQ", schema = SchemaName.DPST, sequenceName = "DEPOSIT_ID_SEQ", allocationSize = 1)
@Data
public class DepositEntity extends TenantAwareEntity {
    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPOSIT_ID_SEQ")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای سپرده")
    private Long id;

    @Column(name = "DEPOSIT_NUMBER", nullable = false)
    @Comment("شماره سپرده")
    private String depositNumber;

    @Column(name = "PRODUCT_CODE", nullable = false)
    @Comment("کد محصول سپرده")
    private String productCode;

    @Column(name = "DEPOSIT_STATUS")
    @Enumerated(EnumType.ORDINAL)
    @Comment("وضعیت سپرده")
    private DepositStatus depositStatus;

    @Column(name = "BALANCE")
    @Comment("مانده سپرده")
    private BigDecimal balance;

//    @Column(name = "BLOCK_AMOUNT")
//    @Comment("مبلغ مسدودی")
//    private BigDecimal blockAmount;

    @Column(name = "DESCRIPTION")
    @Comment("توضیحات")
    private String description;

    @Column(name = "CUSTOMER_ID")
    @Comment("شناسه مشتری صاحب سپرده")
    private long customerId;

}
