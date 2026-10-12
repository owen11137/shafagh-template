package com.misc.shafagh.cif.impl.entity.fund;

import com.misc.shafagh.cif.impl.entity.CityEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = CifSchema.NAME, name = "FUND_ADDRESS")
@SequenceGenerator(name = "FUND_ADDRESS_ID_SEQ", schema = CifSchema.NAME, sequenceName = "FUND_ADDRESS_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class FundAddressEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FUND_ADDRESS_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "FUND_ID", referencedColumnName = "ID", nullable = false)
    private FundEntity fundEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FUND_ADDRESS_FK_CITY"))
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
