package com.misc.shafagh.cif.impl.entity;

import com.misc.client.infra.starter.v3.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


@MappedSuperclass
@Getter
@Setter
public abstract class TenantAwareEntity extends BaseEntity {
    @Column(name = "FUND_ID")
    private Long fundId;
}
