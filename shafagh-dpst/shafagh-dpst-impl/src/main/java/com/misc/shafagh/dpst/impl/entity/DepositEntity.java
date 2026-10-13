package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "Deposit")
@Table(name = "DEPOSIT", schema = SchemaName.DP, comment = "جدول سپرده‌ها")
@SequenceGenerator(name = "SEQ_DEPOSIT_ID", schema = SchemaName.DP, sequenceName = "SEQ_DEPOSIT_ID", allocationSize = 1)
@Data
public class DepositEntity extends TenantAwareEntity {
    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای سپرده")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DEPOSIT_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "DEPOSIT_NUMBER", nullable = false, comment = "شماره سپرده")
    private String depositNumber;

    @Column(name = "PRODUCT_CODE", nullable = false, comment = "کد محصول سپرده")
    private String productCode;

    @Column(name = "DEPOSIT_STATUS", comment = "وضعیت سپرده")
    @Enumerated(EnumType.ORDINAL)
    private DepositStatus depositStatus;

    @Column(name = "BALANCE", comment = "مانده سپرده")
    private BigDecimal balance;

//    @Column(name = "BLOCK_AMOUNT", comment = "مبلغ مسدودی")

//    private BigDecimal blockAmount;

    @Column(name = "DESCRIPTION", comment = "توضیحات")
    private String description;

    @Column(name = "CUSTOMER_ID", comment = "شناسه مشتری صاحب سپرده")
    private long customerId;

}
