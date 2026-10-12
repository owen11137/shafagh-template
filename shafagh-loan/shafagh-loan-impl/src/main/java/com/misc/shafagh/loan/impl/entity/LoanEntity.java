package com.misc.shafagh.loan.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity(name = "Loan")
@Table(name = "LOAN", schema = SchemaName.LOAN)
@Comment("جدول وام‌ها (تسهیلات)")
@SequenceGenerator(schema = SchemaName.LOAN, name = "LOAN_ID_SEQ", sequenceName = "LOAN_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class LoanEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOAN_ID_SEQ")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای وام")
    private Long id;

    @Column(name = "LOAN_NUMBER", nullable = false)
    @Comment("شماره وام")
    private String loanNumber;

    @Column(name = "PRODUCT_CODE", nullable = false)
    @Comment("کد محصول وام")
    private String productCode;

    @Column(name = "AMOUNT", nullable = false, precision = 18, scale = 2)
    @Comment("مبلغ وام")
    private BigDecimal amount;

    @Column(name = "INSTALLMENT_COUNT", nullable = false)
    @Comment("تعداد اقساط")
    private Integer installmentCount;

    @Column(name = "FEE_RATE", precision = 5, scale = 2)
    @Comment("نرخ کارمزد")
    private BigDecimal feeRate;

    @Column(name = "FINE_RATE", precision = 5, scale = 2)
    @Comment("نرخ جریمه")
    private BigDecimal fineRate;

    @Column(name = "DESCRIPTION")
    @Comment("شرح")
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "LOAN_STATUS")
    @Comment("وضعیت")
    private LoanStatus loanStatus;

    @Column(name = "GRANT_NUMBER")
    @Comment("شماره اعطا")
    private String grantNumber;

    @Column(name = "SOURCE_NUMBER")
    @Comment("شماره سپرده وجوه")
    private String sourceNumber;

    @Column(name = "BALANCE", precision = 18, scale = 2)
    @Comment("مانده تسهیلات")
    private BigDecimal balance;

    @Column(name = "SETTLEMENT_BALANCE", precision = 18, scale = 2)
    @Comment("مانده تسویه")
    private BigDecimal settlementBalance;
}
