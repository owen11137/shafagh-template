package com.misc.shafagh.loan.impl.entity;

import com.misc.shafagh.loan.impl.LoanSchema;
import com.misc.shafagh.loan.api.dto.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(schema = LoanSchema.NAME, name = "SHFQ_LOAN_LOAN")
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shfq_loan_loan_gen")
    @SequenceGenerator(name = "shfq_loan_loan_gen", schema = LoanSchema.NAME, sequenceName = "SHFQ_LOAN_LOAN_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    @Column(name = "DEPOSIT_ID", nullable = false)
    private Long depositId;

    @Column(name = "AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private LoanStatus status;

    protected LoanEntity() {
    }

    public LoanEntity(Long personId, Long depositId, BigDecimal amount) {
        this.personId = personId;
        this.depositId = depositId;
        this.amount = amount;
        this.status = LoanStatus.REGISTERED;
    }

    public Long getId() { return id; }
    public Long getPersonId() { return personId; }
    public Long getDepositId() { return depositId; }
    public BigDecimal getAmount() { return amount; }
    public LoanStatus getStatus() { return status; }
}
