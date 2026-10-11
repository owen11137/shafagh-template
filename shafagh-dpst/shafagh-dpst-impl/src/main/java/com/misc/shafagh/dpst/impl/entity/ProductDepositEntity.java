package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.base.api.dto.ProductStatus;
import com.misc.shafagh.base.persistence.entity.TenantAwareEntity;
import com.misc.shafagh.dpst.api.dto.DepositType;
import com.misc.shafagh.dpst.impl.DpstSchema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(schema = DpstSchema.NAME, name = "PRODUCT_DEPOSIT")
@Comment("جدول محصولات سپرده")
@SequenceGenerator(name = "PRODUCT_DEPOSIT_ID_SEQ", schema = DpstSchema.NAME,
        sequenceName = "PRODUCT_DEPOSIT_ID_SEQ", allocationSize = 1)
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
