package com.misc.shafagh.base.impl.entity.branch;

import org.hibernate.annotations.Comment;
import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.base.impl.entity.CityEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Comment("جدول نشانی شعبه")
@Entity
@Table(schema = SchemaName.BASE, name = "BRANCH_ADDRESS")
@SequenceGenerator(name = "SEQ_BRANCH_ADDRESS_ID", schema = SchemaName.BASE, sequenceName = "SEQ_BRANCH_ADDRESS_ID", allocationSize = 1)
@Getter
@Setter
public class BranchAddressEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BRANCH_ADDRESS_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد نشانی شعبه")
    private Long id;

    @OneToOne
    @JoinColumn(name = "BRANCH_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_BRANCH_ADDRESS_BRANCH"))
    @Comment("شناسه شعبه مرتبط با رکورد")
    private BranchEntity branchEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "FK_BRANCH_ADDRESS_CITY"))
    @Comment("شناسه شهر مرتبط با رکورد")
    private CityEntity city;

    @Column(name = "MAIN_STREET")
    @Comment("نام خیابان اصلی نشانی")
    private String mainStreet;

    @Column(name = "STREET")
    @Comment("نام خیابان نشانی")
    private String street;

    @Column(name = "ALLEY")
    @Comment("نام کوچه نشانی")
    private String alley;

    @Column(name = "TAG")
    @Comment("پلاک نشانی")
    private String tag;

    @Column(name = "POSTAL_CODE")
    @Comment("کد پستی نشانی")
    private String postalCode;

    @Column(name = "UNIT")
    @Comment("شماره واحد نشانی")
    private String unit;
}
