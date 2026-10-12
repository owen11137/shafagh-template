package com.misc.shafagh.cif.impl.entity.branch;

import com.misc.shafagh.cif.impl.entity.CityEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = CifSchema.NAME, name = "BRANCH_ADDRESS")
@SequenceGenerator(name = "BRANCH_ADDRESS_ID_SEQ", schema = CifSchema.NAME, sequenceName = "BRANCH_ADDRESS_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class BranchAddressEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BRANCH_ADDRESS_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "BRANCH_ID", referencedColumnName = "ID", nullable = false)
    private BranchEntity branchEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "BRANCH_ADDRESS_FK_CITY"))
    private CityEntity city;

    @Column(name = "MAIN_STREET")
    private String mainStreet;

    @Column(name = "STREET")
    private String street;

    @Column(name = "ALLEY")
    private String alley;

    @Column(name = "TAG")
    private String tag;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "UNIT")
    private String unit;
}
