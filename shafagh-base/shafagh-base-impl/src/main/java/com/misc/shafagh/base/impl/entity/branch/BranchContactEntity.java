package com.misc.shafagh.base.impl.entity.branch;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.common.contact.ContactType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(schema = SchemaName.BASE, name = "BRANCH_CONTACT", comment = "جدول راه‌های ارتباطی شعبه")
@Getter
@Setter
@SequenceGenerator(name = "SEQ_BRANCH_CONTACT_ID", schema = SchemaName.BASE, sequenceName = "SEQ_BRANCH_CONTACT_ID", allocationSize = 1)
public class BranchContactEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای رکورد راه‌های ارتباطی شعبه")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BRANCH_CONTACT_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CONTACT_TYPE", comment = "نوع راه ارتباطی")
    @Enumerated(EnumType.ORDINAL)
    private ContactType contactType;

    @Column(name = "CONTACT_NUMBER", comment = "شماره تماس")
    private String contactNumber;

    @Column(name = "REGION_CODE", comment = "پیش‌شماره منطقه تماس")
    private int regionCode;

    @Column(name = "INTERNAL", comment = "شماره داخلی تلفن")
    private String internal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FK_BRANCH_CONTACT_BRANCH"), nullable = false, comment = "شناسه شعبه مرتبط با رکورد")
    private BranchEntity branch;
}
