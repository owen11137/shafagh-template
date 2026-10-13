package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT_DEPOSIT", schema = SchemaName.DP, uniqueConstraints = {@UniqueConstraint(name = "UK_PRODUCT_DEPOSIT_CODE", columnNames = "CODE")}, comment = "جدول محصولات سپرده")
@SequenceGenerator(name = "SEQ_PRODUCT_DEPOSIT_ID", schema = SchemaName.DP, sequenceName = "SEQ_PRODUCT_DEPOSIT_ID", allocationSize = 1)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductDepositEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", comment = "شناسه یکتای محصول سپرده")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PRODUCT_DEPOSIT_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "TITLE", comment = "عنوان محصول")
    private String title;

    @Nationalized
    @Column(name = "CODE", nullable = false, comment = "کد محصول")
    private String code;

    @Nationalized
    @Column(name = "DESCRIPTION", comment = "توضیحات")
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS", comment = "وضعیت محصول")
    private ProductStatus status;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "PRODUCT_CATEGORY_ID", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "PRODUCT_FK_PRODUCT_CATEGORY"), comment = "شناسه دسته‌بندی محصول")

//    private ProductCategoryEntity productCategory;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "DEPOSIT_TYPE", nullable = false, comment = "نوع سپرده")
    private DepositType depositType;

    @Column(name = "FROM_DATE", comment = "تاریخ شروع اعتبار محصول")
    private LocalDateTime fromDate;

    @Column(name = "TO_DATE", comment = "تاریخ پایان اعتبار محصول")
    private LocalDateTime toDate;
}
