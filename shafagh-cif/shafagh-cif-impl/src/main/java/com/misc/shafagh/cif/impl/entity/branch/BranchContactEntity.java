package com.misc.shafagh.cif.impl.entity.branch;

import com.misc.shafagh.core.entity.enums.ContactType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
@Entity
@Table(name = "BRANCH_CONTACT")
@Getter
@Setter
@SequenceGenerator(name = "BRANCH_CONTACT_ID_SEQ", sequenceName = "BRANCH_CONTACT_ID_SEQ", allocationSize = 1)
public class BranchContactEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BRANCH_CONTACT_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "CONTACT_TYPE")
    @Enumerated(EnumType.ORDINAL)
    private ContactType contactType;

    @Column(name = "CONTACT_NUMBER")
    private String contactNumber;

    @Column(name = "REGION_CODE")
    private int regionCode;

    @Column(name = "INTERNAL")
    private String internal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID", referencedColumnName = "ID", foreignKey = @ForeignKey(name = "BRANCH_CONTACT_FK_BRANCH"), nullable = false)
    private BranchEntity branch;
}
