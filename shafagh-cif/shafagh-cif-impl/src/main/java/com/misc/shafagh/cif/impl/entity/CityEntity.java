package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.common.SchemaName;
import jakarta.persistence.*;


@Entity
@Table(schema = SchemaName.BASE, name = "CITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "CITY_ID_SEQ", schema = CifSchema.NAME, sequenceName = "CITY_ID_SEQ", allocationSize = 1)
public class CityEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CITY_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Basic
    @Column(name = "NAME",  length = 255)
    private String name;

    @Basic
    @Column(name = "PHONE_CODE",  length = 255)
    private String phoneCode;

    @Basic
    @Column(name = "CODE",  length = 255)
    private String code;

    @Column(name = "PARENT_ID", columnDefinition = "number(19,0)")
    private Long parentId;

}
