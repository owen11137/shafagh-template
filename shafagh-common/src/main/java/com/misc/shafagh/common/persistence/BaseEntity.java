package com.misc.shafagh.common.persistence;

import org.hibernate.annotations.Comment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity implements Serializable, Cloneable {

    protected static final Timestamp DELETE_AT = Timestamp.valueOf("1970-01-01 00:00:00.0000000");

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = true, updatable = false, length = 30)
    @Comment("شناسه کاربر ایجادکننده رکورد")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_ON", nullable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSS")
    @Comment("زمان ایجاد رکورد")
    private Date createdOn;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY", nullable = true, length = 30)
    @Comment("شناسه آخرین کاربر ویرایش‌کننده رکورد")
    private String modifiedBy;

    @LastModifiedDate
    @Column(name = "MODIFIED_ON", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSSS")
    @Comment("زمان آخرین ویرایش رکورد")
    private Date modifiedOn;

    @JsonIgnore
    @Version
    @Column(name = "OPT_LOCK", nullable = true)
    @ColumnDefault("0")
    @Setter(onMethod_ = @JsonIgnore)
    @Comment("نسخه رکورد برای کنترل هم‌زمانی و قفل خوش‌بینانه")
    private Long version = 0L;
}
