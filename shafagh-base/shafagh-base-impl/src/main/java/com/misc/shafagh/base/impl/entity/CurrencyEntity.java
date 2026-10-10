package com.misc.shafagh.base.impl.entity;

import com.misc.shafagh.base.impl.BaseSchema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = BaseSchema.NAME, name = "SHFQ_BASE_CURRENCY")
public class CurrencyEntity {

    @Id
    @Column(name = "CODE", length = 3)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    protected CurrencyEntity() {
    }

    public CurrencyEntity(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
