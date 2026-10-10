package com.misc.shafagh.cif.impl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


@MappedSuperclass
@Getter
@Setter
public abstract class TenantAwareEntity  {
    @Column(name = "FUND_ID")
    private Long fundId;
}
