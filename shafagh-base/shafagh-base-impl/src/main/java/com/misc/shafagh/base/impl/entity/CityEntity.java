package com.misc.shafagh.base.impl.entity;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;
import lombok.*;


@Comment("جدول شهرها و استان‌ها")
@Entity
@Table(schema = SchemaName.BASE, name = "CITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "SEQ_CITY_ID", schema = SchemaName.BASE, sequenceName = "SEQ_CITY_ID", allocationSize = 1)
public class CityEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CITY_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد شهرها و استان‌ها")
    private Long id;

    @Basic
    @Column(name = "NAME",  length = 255)
    @Comment("نام شهر یا استان")
    private String name;

    @Basic
    @Column(name = "PHONE_CODE",  length = 255)
    @Comment("پیش‌شماره تلفن شهر")
    private String phoneCode;

    @Basic
    @Column(name = "CODE",  length = 255)
    @Comment("کد شهر یا استان")
    private String code;

    @Column(name = "PARENT_ID", columnDefinition = "number(19,0)")
    @Comment("شناسه والد جغرافیایی؛ مقدار صفر نشان‌دهنده استان است")
    private Long parentId;

}
