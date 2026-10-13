package com.misc.shafagh.base.impl.entity.fund;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Comment("جدول صندوق‌ها")
@Entity(name = "FUND")
@Table(schema = SchemaName.BASE, name = "FUND", uniqueConstraints = {@UniqueConstraint(name = "UK_FUND_OWNER_ID", columnNames = "OWNER_ID")})
@Getter
@Setter
@SequenceGenerator(name = "SEQ_FUND_ID", schema = SchemaName.BASE, sequenceName = "SEQ_FUND_ID", allocationSize = 1)
public class FundEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FUND_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد صندوق‌ها")
    private Long id;

    @Column(name = "CODE", nullable = false)
    @Comment("کد صندوق")
    private String code;

    @Column(name = "NAME", nullable = false)
    @Comment("نام صندوق")
    private String name;

    @Column(name = "OWNER_ID", nullable = false)
    @Comment("شناسه مشتری مالک صندوق در ماژول CIF")
    private Long ownerId;

    @Basic
    @Column(name = "TO_DATE")
    @Comment("تاریخ پایان اعتبار رکورد")
    private LocalDateTime toDate;

    @OneToMany(mappedBy = "fund")
    private List<BranchEntity> branches = new ArrayList<>();

}
