package com.misc.shafagh.loan.impl.entity;

import com.misc.shafagh.common.SchemaName;
import com.misc.shafagh.common.persistence.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity(name = "LoanPerson")
@Table(name = "LOAN_PERSON", schema = SchemaName.LN, comment = "جدول اشخاص مرتبط با وام")
@SequenceGenerator(schema = SchemaName.LN, name = "SEQ_LOAN_PERSON_ID", sequenceName = "SEQ_LOAN_PERSON_ID", allocationSize = 1)
@Getter
@Setter
public class LoanPersonEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای شخص وام")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_LOAN_PERSON_ID")
    @EqualsAndHashCode.Include
    private Long id;

    // CIF is accessed through its API; no cross-module JPA association.
    @Column(name = "CUSTOMER_ID", nullable = false, comment = "شناسه مشتری در ماژول CIF")
    private Long customerId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LOAN_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_LOAN_PERSON_LOAN"), comment = "شناسه وام")
    private LoanEntity loan;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "LOAN_PERSON_TYPE", nullable = false, comment = "نقش شخص در وام (ضامن, صاحب)")
    private LoanPersonType loanPersonType = LoanPersonType.OWNER;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "COLLATERAL_TYPE", comment = "نوع وثیقه")
    private CollateralType collateralType;

    @Column(name = "COLLATERAL_NUMBER", comment = "شماره وثیقه")
    private String collateralNumber;

    @Column(name = "DESCRIPTION", comment = "توضیحات")
    private String description;
}
