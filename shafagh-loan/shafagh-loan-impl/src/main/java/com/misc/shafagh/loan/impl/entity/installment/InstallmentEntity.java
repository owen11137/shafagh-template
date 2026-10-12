package com.misc.shafagh.loan.impl.entity.installment;

import com.misc.shafagh.loan.impl.entity.LoanEntity;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "Installment")
@Table(name = "INSTALLMENT", schema = SchemaName.LN)
@Comment("جدول اقساط")
@SequenceGenerator(schema = SchemaName.LN, name = "SEQ_INSTALLMENT_ID", sequenceName = "SEQ_INSTALLMENT_ID", allocationSize = 1)
@Getter
@Setter
public class InstallmentEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INSTALLMENT_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای قسط")
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "INSTALLMENT_TYPE", nullable = false)
    @Comment("نوع قسط")
    private InstallmentType installmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_INSTALLMENT_LOAN"))
    @Comment("شناسه وام")
    private LoanEntity loan;

    @Column(name = "INSTALLMENT_NUMBER", nullable = false)
    @Comment("شماره قسط")
    private int installmentNumber;

    @Column(name = "INSTALLMENT_AMOUNT", nullable = false)
    @Comment("مبلغ قسط")
    private BigDecimal installmentAmount;

    @Column(name = "INSTALLMENT_DATE", nullable = false)
    @Comment("تاریخ سررسید قسط")
    private LocalDateTime installmentDate;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "INSTALLMENT_STATUS", nullable = false)
    @Comment("وضعیت قسط")
    private InstallmentStatus installmentStatus;
}
