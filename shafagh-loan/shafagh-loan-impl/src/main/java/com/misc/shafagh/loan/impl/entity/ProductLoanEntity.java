package com.misc.shafagh.loan.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;
import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.product.ProductStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "ProductLoan")
@Table(name = "PRODUCT_LOAN", schema = SchemaName.LOAN)
@Comment("جدول محصولات وام")
@SequenceGenerator(schema = SchemaName.LOAN, name = "PRODUCT_LOAN_SEQUENCE", sequenceName = "PRODUCT_LOAN_SEQUENCE", allocationSize = 1)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductLoanEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_LOAN_SEQUENCE")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای محصول وام")
    private Long id;

    @Nationalized
    @Column(name = "TITLE", length = 300)
    @Comment("عنوان محصول")
    private String title;

    @Nationalized
    @Column(name = "CODE", length = 20, unique = true, nullable = false)
    @Comment("کد محصول")
    private String code;

    @Nationalized
    @Column(name = "DESCRIPTION", length = 300)
    @Comment("توضیحات")
    private String description;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_CATEGORY_ID", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "PRODUCT_FK_PRODUCT_CATEGORY"))
    @Comment("شناسه دسته‌بندی محصول")
    private ProductCategoryEntity productCategory;*/

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "PRODUCT_TYPE", nullable = false)
    @Comment("نوع وام")
    private LoanType productType;

    @Column(name = "FROM_DATE")
    @Comment("تاریخ شروع اعتبار محصول")
    private LocalDateTime fromDate;

    @Column(name = "TO_DATE")
    @Comment("تاریخ پایان اعتبار محصول")
    private LocalDateTime toDate;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS")
    @Comment("وضعیت محصول")
    private ProductStatus status;

    @Column(name = "MIN_AMOUNT")
    @Comment("حداقل مبلغ وام")
    private BigDecimal minAmount;

    @Column(name = "MAX_AMOUNT")
    @Comment("حداکثر مبلغ وام")
    private BigDecimal maxAmount;

    @Column(name = "MIN_FEE")
    @Comment("حداقل کارمزد")
    private BigDecimal minFee;

    @Column(name = "MAX_FEE")
    @Comment("حداکثر کارمزد")
    private BigDecimal maxFee;

    @Column(name = "MIN_FINE")
    @Comment("حداقل جریمه دیرکرد")
    private BigDecimal minFine;

    @Column(name = "MAX_FINE")
    @Comment("حداکثر جریمه دیرکرد")
    private BigDecimal maxFine;

    @Column(name = "INSTALLMENT_INTERVAL")
    @Comment("فاصله بین اقساط")
    private int installmentInterval;

    @Column(name = "MIN_INSTALLMENT_COUNT")
    @Comment("حداقل تعداد اقساط")
    private int minInstallmentCount;

    @Column(name = "MAX_INSTALLMENT_COUNT")
    @Comment("حداکثر تعداد اقساط")
    private int maxInstallmentCount;

    @Column(name = "EARLY_SETTLEMENT_ALLOWED", nullable = false)
    @Comment("امکان تسویه زودهنگام")
    private Boolean earlySettlementAllowed;
}
