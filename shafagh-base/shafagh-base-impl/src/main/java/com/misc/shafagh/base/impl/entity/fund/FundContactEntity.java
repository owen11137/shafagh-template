package com.misc.shafagh.base.impl.entity.fund;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import com.misc.shafagh.common.contact.ContactType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = SchemaName.BASE, name = "FUND_CONTACT")
@Getter
@Setter
@SequenceGenerator(name = "FUND_CONTACT_ID_SEQ", schema = SchemaName.BASE, sequenceName = "FUND_CONTACT_ID_SEQ", allocationSize = 1)
public class FundContactEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FUND_CONTACT_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CONTACT_TYPE")
    @Enumerated(EnumType.ORDINAL)
    private ContactType contactType;

    @Column(name = "CONTACT_NUMBER")
    private String contactNumber;

    @ManyToOne
    @JoinColumn(name = "FUND_ID", referencedColumnName = "ID", nullable = false)
    private FundEntity fundEntity;

    @Column(name = "REGION_CODE")
    private int regionCode;

    @Column(name = "INTERNAL")
    private String internal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "FUND_CONTACT_FK_BRANCH"), nullable = false)
    private BranchEntity branch;
}
