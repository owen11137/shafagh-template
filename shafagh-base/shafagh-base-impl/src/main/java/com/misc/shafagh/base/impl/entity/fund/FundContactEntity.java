package com.misc.shafagh.base.impl.entity.fund;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import com.misc.shafagh.common.contact.ContactType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Comment("جدول راه‌های ارتباطی صندوق")
@Entity
@Table(schema = SchemaName.BASE, name = "FUND_CONTACT")
@Getter
@Setter
@SequenceGenerator(name = "SEQ_FUND_CONTACT_ID", schema = SchemaName.BASE, sequenceName = "SEQ_FUND_CONTACT_ID", allocationSize = 1)
public class FundContactEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FUND_CONTACT_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد راه‌های ارتباطی صندوق")
    private Long id;

    @Column(name = "CONTACT_TYPE")
    @Enumerated(EnumType.ORDINAL)
    @Comment("نوع راه ارتباطی")
    private ContactType contactType;

    @Column(name = "CONTACT_NUMBER")
    @Comment("شماره تماس")
    private String contactNumber;

    @ManyToOne
    @JoinColumn(name = "FUND_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_FUND_CONTACT_FUND"))
    @Comment("شناسه صندوق مرتبط با رکورد")
    private FundEntity fundEntity;

    @Column(name = "REGION_CODE")
    @Comment("پیش‌شماره منطقه تماس")
    private int regionCode;

    @Column(name = "INTERNAL")
    @Comment("شماره داخلی تلفن")
    private String internal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_FUND_CONTACT_BRANCH"), nullable = false)
    @Comment("شناسه شعبه مرتبط با رکورد")
    private BranchEntity branch;
}
