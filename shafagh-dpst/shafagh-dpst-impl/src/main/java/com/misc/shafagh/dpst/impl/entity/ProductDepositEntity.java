package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.product.ProductStatus;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT_DEPOSIT", schema = SchemaName.DPST)
@Comment("جدول محصولات سپرده")
@SequenceGenerator(name = "PRODUCT_DEPOSIT_ID_SEQ", sequenceName = "PRODUCT_DEPOSIT_ID_SEQ", allocationSize = 1)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductDepositEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PRODUCT_DEPOSIT_ID_SEQ")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای محصول سپرده")
    private Long id;

    @Column(name = "TITLE")
    @Comment("عنوان محصول")
    private String title;

    @Nationalized
    @Column(name = "CODE", unique = true, nullable = false)
    @Comment("کد محصول")
    private String code;

    @Nationalized
    @Column(name = "DESCRIPTION")
    @Comment("توضیحات")
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "STATUS")
    @Comment("وضعیت محصول")
    private ProductStatus status;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "PRODUCT_CATEGORY_ID", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "PRODUCT_FK_PRODUCT_CATEGORY"))
//    @Comment("شناسه دسته‌بندی محصول")
//    private ProductCategoryEntity productCategory;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "DEPOSIT_TYPE", nullable = false)
    @Comment("نوع سپرده")
    private DepositType depositType;

    @Column(name = "FROM_DATE")
    @Comment("تاریخ شروع اعتبار محصول")
    private LocalDateTime fromDate;

    @Column(name = "TO_DATE")
    @Comment("تاریخ پایان اعتبار محصول")
    private LocalDateTime toDate;
}
