package com.misc.shafagh.cif.impl.entity.branch;

import com.misc.shafagh.cif.impl.CifSchema;

import com.misc.shafagh.cif.impl.entity.fund.FundEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collection;

@Entity(name = "BRANCH")
@Table(schema = CifSchema.NAME, uniqueConstraints = {@UniqueConstraint(name = "BRANCH_IDENTIFIER_UK", columnNames = "IDENTIFIER")})
@Getter
@Setter
@Data
@SequenceGenerator(name = "BRANCH_ID_SEQ", schema = CifSchema.NAME, sequenceName = "BRANCH_ID_SEQ", allocationSize = 1)
public class BranchEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BRANCH_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "NAME", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "FUND_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "BRANCH_FK_FUND"))
    private FundEntity fund;

    @Basic
    @Column(name = "TO_DATE")
    private LocalDateTime toDate;

    @OneToMany(mappedBy = "branch")
    private Collection<BranchContactEntity> branchContactList;
}
