package com.misc.shafagh.base.impl.entity.fund;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.CityEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = SchemaName.BASE, name = "FUND_ADDRESS", comment = "جدول نشانی صندوق")
@SequenceGenerator(name = "SEQ_FUND_ADDRESS_ID", schema = SchemaName.BASE, sequenceName = "SEQ_FUND_ADDRESS_ID", allocationSize = 1)
@Getter
@Setter
public class FundAddressEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای رکورد نشانی صندوق")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FUND_ADDRESS_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne
    @JoinColumn(name = "FUND_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_FUND_ADDRESS_FUND"), comment = "شناسه صندوق مرتبط با رکورد")
    private FundEntity fundEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_FUND_ADDRESS_CITY"), comment = "شناسه شهر مرتبط با رکورد")
    private CityEntity city;

    @Column(name = "MAIN_STREET", comment = "نام خیابان اصلی نشانی")
    private String mainStreet;

    @Column(name = "STREET", comment = "نام خیابان نشانی")
    private String street;

    @Column(name = "ALLEY", comment = "نام کوچه نشانی")
    private String alley;

    @Column(name = "TAG", comment = "پلاک نشانی")
    private String tag;

    @Column(name = "POSTAL_CODE", comment = "کد پستی نشانی")
    private String postalCode;

    @Column(name = "UNIT", comment = "شماره واحد نشانی")
    private String unit;


}
