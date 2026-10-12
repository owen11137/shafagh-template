package com.misc.shafagh.base.impl.entity.fund;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.CityEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = SchemaName.BASE, name = "FUND_ADDRESS")
@SequenceGenerator(name = "SEQ_FUND_ADDRESS_ID", schema = SchemaName.BASE, sequenceName = "SEQ_FUND_ADDRESS_ID", allocationSize = 1)
@Getter
@Setter
public class FundAddressEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FUND_ADDRESS_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "FUND_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_FUND_ADDRESS_FUND"))
    private FundEntity fundEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_FUND_ADDRESS_CITY"))
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
