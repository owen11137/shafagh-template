package com.misc.shafagh.base.impl.entity.fund;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "FUND")
@Table(schema = SchemaName.BASE, name = "FUND", uniqueConstraints = {@UniqueConstraint(name = "UK_FUND_OWNER_ID", columnNames = "OWNER_ID")}, comment = "جدول صندوق‌ها")
@Getter
@Setter
@SequenceGenerator(name = "SEQ_FUND_ID", schema = SchemaName.BASE, sequenceName = "SEQ_FUND_ID", allocationSize = 1)
public class FundEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای رکورد صندوق‌ها")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FUND_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CODE", nullable = false, comment = "کد صندوق")
    private String code;

    @Column(name = "NAME", nullable = false, comment = "نام صندوق")
    private String name;

    @Column(name = "OWNER_ID", nullable = false, comment = "شناسه مشتری مالک صندوق در ماژول CIF")
    private Long ownerId;

    @Basic
    @Column(name = "TO_DATE", comment = "تاریخ پایان اعتبار رکورد")
    private LocalDateTime toDate;

    @OneToMany(mappedBy = "fund")
    private List<BranchEntity> branches = new ArrayList<>();

}
