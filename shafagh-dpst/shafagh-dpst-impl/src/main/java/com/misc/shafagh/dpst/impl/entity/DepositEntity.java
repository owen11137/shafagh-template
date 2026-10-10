package com.misc.shafagh.dpst.impl.entity;

import com.misc.shafagh.dpst.impl.DpstSchema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(schema = DpstSchema.NAME, name = "SHFQ_DPST_DEPOSIT")
public class DepositEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shfq_dpst_deposit_gen")
    @SequenceGenerator(name = "shfq_dpst_deposit_gen", schema = DpstSchema.NAME, sequenceName = "SHFQ_DPST_DEPOSIT_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    /** فقط شناسه‌ی شخص؛ هیچ رابطه‌ی JPA به جدول ماژول دیگر وجود ندارد. */
    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    @Column(name = "CURRENCY_CODE", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "BALANCE", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "BLOCKED_AMOUNT", nullable = false, precision = 19, scale = 2)
    private BigDecimal blockedAmount = BigDecimal.ZERO;

    /** قفل خوش‌بینانه: جلوی بلوکه‌کردن هم‌زمانِ بیش از موجودی را می‌گیرد. */
    @Version
    @Column(name = "ROW_VERSION", nullable = false)
    private long version;

    protected DepositEntity() {
    }

    public DepositEntity(Long personId, String currencyCode, BigDecimal balance) {
        this.personId = personId;
        this.currencyCode = currencyCode;
        this.balance = balance;
    }

    public BigDecimal availableAmount() {
        return balance.subtract(blockedAmount);
    }

    public void block(BigDecimal amount) {
        this.blockedAmount = this.blockedAmount.add(amount);
    }

    public Long getId() { return id; }
    public Long getPersonId() { return personId; }
    public String getCurrencyCode() { return currencyCode; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getBlockedAmount() { return blockedAmount; }
}
