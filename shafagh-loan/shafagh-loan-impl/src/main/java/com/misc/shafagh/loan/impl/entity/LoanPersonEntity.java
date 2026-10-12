package com.misc.shafagh.loan.impl.entity;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity(name = "LoanPerson")
@Table(name = "LOAN_PERSON", schema = SchemaName.LN)
@Comment("جدول اشخاص مرتبط با وام")
@SequenceGenerator(schema = SchemaName.LN, name = "LOAN_PERSON_ID_SEQ", sequenceName = "LOAN_PERSON_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class LoanPersonEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOAN_PERSON_ID_SEQ")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای شخص وام")
    private Long id;

    // CIF is accessed through its API; no cross-module JPA association.
    @Column(name = "CUSTOMER_ID", nullable = false)
    @Comment("شناسه مشتری در ماژول CIF")
    private Long customerId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", referencedColumnName = "ID", nullable = false)
    @Comment("شناسه وام")
    private LoanEntity loan;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "LOAN_PERSON_TYPE", nullable = false)
    @Comment("نقش شخص در وام (ضامن, صاحب)")
    private LoanPersonType loanPersonType = LoanPersonType.OWNER;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "COLLATERAL_TYPE")
    @Comment("نوع وثیقه")
    private CollateralType collateralType;

    @Column(name = "COLLATERAL_NUMBER")
    @Comment("شماره وثیقه")
    private String collateralNumber;

    @Column(name = "DESCRIPTION")
    @Comment("توضیحات")
    private String description;
}
