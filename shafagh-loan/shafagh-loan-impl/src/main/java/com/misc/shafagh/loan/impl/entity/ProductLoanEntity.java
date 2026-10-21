package com.misc.shafagh.loan.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;
import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.product.ProductStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "ProductLoan")
@Table(name = "PRODUCT_LOAN", schema = SchemaName.LN, uniqueConstraints = {@UniqueConstraint(name = "UK_PRODUCT_LOAN_CODE", columnNames = "CODE")}, comment = "جدول محصولات وام")
@SequenceGenerator(schema = SchemaName.LN, name = "SEQ_PRODUCT_LOAN_ID", sequenceName = "SEQ_PRODUCT_LOAN_ID", allocationSize = 1)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductLoanEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", comment = "شناسه یکتای محصول وام")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PRODUCT_LOAN_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Nationalized
    @Column(name = "TITLE", length = 300, comment = "عنوان محصول")
    private String title;

    @Nationalized
    @Column(name = "CODE", length = 20, nullable = false, comment = "کد محصول")
    private String code;

    @Nationalized
    @Column(name = "DESCRIPTION", length = 300, comment = "توضیحات")
    private String description;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_CATEGORY_ID", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "PRODUCT_FK_PRODUCT_CATEGORY"), comment = "شناسه دسته‌بندی محصول")
    private ProductCategoryEntity productCategory;*/

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "PRODUCT_TYPE", nullable = false, comment = "نوع وام")
    private LoanType productType;

    @Column(name = "FROM_DATE", comment = "تاریخ شروع اعتبار محصول")
    private LocalDateTime fromDate;

    @Column(name = "TO_DATE", comment = "تاریخ پایان اعتبار محصول")
    private LocalDateTime toDate;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS", comment = "وضعیت محصول")
    private ProductStatus status;

    @Column(name = "MIN_AMOUNT", comment = "حداقل مبلغ وام")
    private BigDecimal minAmount;

    @Column(name = "MAX_AMOUNT", comment = "حداکثر مبلغ وام")
    private BigDecimal maxAmount;

    @Column(name = "MIN_FEE", comment = "حداقل کارمزد")
    private BigDecimal minFee;

    @Column(name = "MAX_FEE", comment = "حداکثر کارمزد")
    private BigDecimal maxFee;

    @Column(name = "MIN_FINE", comment = "حداقل جریمه دیرکرد")
    private BigDecimal minFine;

    @Column(name = "MAX_FINE", comment = "حداکثر جریمه دیرکرد")
    private BigDecimal maxFine;

    @Column(name = "INSTALLMENT_INTERVAL", comment = "فاصله بین اقساط")
    private int installmentInterval;

    @Column(name = "MIN_INSTALLMENT_COUNT", comment = "حداقل تعداد اقساط")
    private int minInstallmentCount;

    @Column(name = "MAX_INSTALLMENT_COUNT", comment = "حداکثر تعداد اقساط")
    private int maxInstallmentCount;

    @Column(name = "EARLY_SETTLEMENT_ALLOWED", nullable = false, comment = "امکان تسویه زودهنگام")
    private Boolean earlySettlementAllowed;
}
