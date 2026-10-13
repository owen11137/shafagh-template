package com.misc.shafagh.cif.impl.entity;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.cif.impl.entity.enums.CustomerInquiryStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Table(schema = SchemaName.CIF, name = "CUSTOMER_INQUIRY")
@Comment("جدول استعلام‌های مشتری")
@Entity(name = "CUSTOMERInquiry")
@SequenceGenerator(name = "SEQ_CUSTOMER_INQUIRY_ID", schema = SchemaName.CIF, sequenceName = "SEQ_CUSTOMER_INQUIRY_ID", allocationSize = 1)
@Getter
@Setter
public class CustomerInquiryEntity extends TenantAwareEntity {
    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CUSTOMER_INQUIRY_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد استعلام‌های مشتری")
    private Long id;

    @Column(name = "CUSTOMER_TYPE", nullable = false)
    @Comment("نوع مشتری؛ حقیقی، حقوقی، حقیقی خارجی یا حقوقی خارجی")
    private CustomerType customerType;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_CUSTOMER_INQUIRY_CUSTOMER"), nullable = false)
    @Comment("شناسه مشتری مرتبط با رکورد")
    private CustomerEntity customer;

    @Column(name = "RESPONSE")
    @Comment("متن پاسخ استعلام مشتری")
    private String response;

    @Column(name = "RESPONSE_VALUE")
    @Comment("مقدار پاسخ استعلام مشتری")
    private int responseValue;

    @Column(name = "RESPONSE_HEADER")
    @Comment("اطلاعات سربرگ پاسخ استعلام مشتری")
    private String responseHeader;

    @Column(name = "STATUS")
    @Enumerated(EnumType.ORDINAL)
    @Comment("وضعیت استعلام مشتری")
    private CustomerInquiryStatus customerInquiryStatus;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_CONTACT_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_CUSTOMER_INQUIRY_CUSTOMER_CONTACT"))
    @Comment("شناسه اطلاعات تماس مشتری مرتبط با استعلام")
    private CustomerContactEntity customerContact;
}


