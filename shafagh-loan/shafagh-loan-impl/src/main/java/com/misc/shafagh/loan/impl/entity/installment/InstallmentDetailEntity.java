package com.misc.shafagh.loan.impl.entity.installment;

import com.misc.shafagh.common.persistence.TenantAwareEntity;
import com.misc.shafagh.loan.impl.entity.LoanEntity;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "InstallmentDetail")
@Table(name = "INSTALLMENT_DETAIL", schema = SchemaName.LN)
@Comment("جدول جزئیات پرداخت اقساط")
@SequenceGenerator(schema = SchemaName.LN, name = "SEQ_INSTALLMENT_DETAIL_ID", sequenceName = "SEQ_INSTALLMENT_DETAIL_ID", allocationSize = 1)
@Getter
@Setter
public class InstallmentDetailEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INSTALLMENT_DETAIL_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای جزئیات قسط")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_INSTALLMENT_DETAIL_LOAN"))
    @Comment("شناسه وام")
    private LoanEntity loan;

    @Column(name = "INSTALLMENT_NUMBER", nullable = false)
    @Comment("شماره قسط")
    private int installmentNumber;

    @Column(name = "PAID_AMOUNT", nullable = false)
    @Comment("مبلغ پرداخت‌شده")
    private BigDecimal paidAmount;

    @Column(name = "PAID_DATE")
    @Comment("تاریخ پرداخت")
    private LocalDateTime paidDate;

    @Column(name = "FINE_AMOUNT")
    @Comment("مبلغ جریمه")
    private BigDecimal fineAmount;

    @Column(name = "TRANSACTION_NO")
    @Comment("شماره تراکنش")
    private long transactionNO;
}
