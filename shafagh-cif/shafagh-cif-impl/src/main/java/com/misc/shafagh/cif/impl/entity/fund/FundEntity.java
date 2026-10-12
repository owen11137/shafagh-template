package com.misc.shafagh.cif.impl.entity.fund;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.cif.impl.entity.CustomerEntity;
import com.misc.shafagh.cif.impl.entity.branch.BranchEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "FUND")
@Table(schema = SchemaName.CIF, name = "FUND")
@Getter
@Setter
@SequenceGenerator(name = "FUND_ID_SEQ", schema = SchemaName.CIF, sequenceName = "FUND_ID_SEQ", allocationSize = 1)
public class FundEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FUND_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "NAME", nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "OWNER_ID", nullable = false, referencedColumnName = "ID")
    private CustomerEntity customer;

    @Basic
    @Column(name = "TO_DATE")
    private LocalDateTime toDate;

    @OneToMany(mappedBy = "fund")
    private List<BranchEntity> branches = new ArrayList<>();

}
