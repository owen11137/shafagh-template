package com.misc.shafagh.loan.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity(name = "Loan")
@Table(name = "LOAN", schema = SchemaName.LN, comment = "جدول وام‌ها (تسهیلات)")
@SequenceGenerator(schema = SchemaName.LN, name = "SEQ_LOAN_ID", sequenceName = "SEQ_LOAN_ID", allocationSize = 1)
@Getter
@Setter
public class LoanEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای وام")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_LOAN_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "LOAN_NUMBER", nullable = false, comment = "شماره وام")
    private String loanNumber;

    @Column(name = "PRODUCT_CODE", nullable = false, comment = "کد محصول وام")
    private String productCode;

    @Column(name = "AMOUNT", nullable = false, precision = 18, scale = 2, comment = "مبلغ وام")
    private BigDecimal amount;

    @Column(name = "INSTALLMENT_COUNT", nullable = false, comment = "تعداد اقساط")
    private Integer installmentCount;

    @Column(name = "FEE_RATE", precision = 5, scale = 2, comment = "نرخ کارمزد")
    private BigDecimal feeRate;

    @Column(name = "FINE_RATE", precision = 5, scale = 2, comment = "نرخ جریمه")
    private BigDecimal fineRate;

    @Column(name = "DESCRIPTION", comment = "شرح")
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "LOAN_STATUS", comment = "وضعیت")
    private LoanStatus loanStatus;

    @Column(name = "GRANT_NUMBER", comment = "شماره اعطا")
    private String grantNumber;

    @Column(name = "SOURCE_NUMBER", comment = "شماره سپرده وجوه")
    private String sourceNumber;

    @Column(name = "BALANCE", precision = 18, scale = 2, comment = "مانده تسهیلات")
    private BigDecimal balance;

    @Column(name = "SETTLEMENT_BALANCE", precision = 18, scale = 2, comment = "مانده تسویه")
    private BigDecimal settlementBalance;
}
