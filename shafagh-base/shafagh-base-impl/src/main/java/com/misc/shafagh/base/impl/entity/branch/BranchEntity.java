package com.misc.shafagh.base.impl.entity.branch;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.fund.FundEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;

@Entity(name = "BRANCH")
@Table(name = "BRANCH", schema = SchemaName.BASE, uniqueConstraints = {@UniqueConstraint(name = "UK_BRANCH_FUND_ID_CODE", columnNames = {"FUND_ID", "CODE"})})
@Getter
@Setter
@SequenceGenerator(name = "SEQ_BRANCH_ID", schema = SchemaName.BASE, sequenceName = "SEQ_BRANCH_ID", allocationSize = 1)
public class BranchEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BRANCH_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FUND_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "BRANCH_FK_FUND"))
    private FundEntity fund;

    @Basic
    @Column(name = "TO_DATE")
    private LocalDateTime toDate;

    @OneToMany(mappedBy = "branch")
    private Collection<BranchContactEntity> branchContactList;
}
