package com.misc.shafagh.loan.impl.dto.mapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.mapstruct.TargetType;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.stereotype.Component;

/** شناسه را به مرجع همان persistence unit تبدیل می‌کند؛ اعتبارسنجی وجود/مالکیت در سرویس انجام می‌شود. */
@Component("loanEntityReferenceResolver")
public class EntityReferenceResolver {
    private final EntityManager entityManager;

    public EntityReferenceResolver(@Qualifier("loanEntityManagerFactory") EntityManagerFactory factory) {
        entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
    }

    @Named("entityReference")
    public <T> T resolve(Long id, @TargetType Class<T> type) {
        return id == null ? null : entityManager.getReference(type, id);
    }
}
