package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.common.persistence.TenantAwareEntity;

import com.misc.shafagh.common.SchemaName;

import com.misc.shafagh.cif.impl.entity.enums.CustomerStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import com.misc.shafagh.cif.impl.entity.enums.Gender;
import com.misc.shafagh.cif.impl.entity.enums.MaritalStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(schema = SchemaName.CIF, name = "CUSTOMER", uniqueConstraints = @UniqueConstraint(name = "UK_CUSTOMER_FUND_ID_IDENTIFIER", columnNames = {"FUND_ID", "IDENTIFIER"}), comment = "جدول مشتریان حقیقی و حقوقی")
@SequenceGenerator(name = "SEQ_CUSTOMER_ID", schema = SchemaName.CIF, sequenceName = "SEQ_CUSTOMER_ID", allocationSize = 1)
@Getter
@Setter
public class CustomerEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)", comment = "شناسه یکتای رکورد مشتریان حقیقی و حقوقی")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CUSTOMER_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @Basic
    @Column(name = "IDENTIFIER", nullable = false, comment = "شناسه شناسایی مشتری؛ شماره ملی، شناسه ملی یا شناسه شخص خارجی بر اساس نوع مشتری")
    private String identifier;

    @Column(name = "CUSTOMER_TYPE", nullable = false, comment = "نوع مشتری؛ حقیقی، حقوقی، حقیقی خارجی یا حقوقی خارجی")
    @Enumerated(EnumType.ORDINAL)
    private CustomerType customerType;

    @Basic
    @Column(name = "REGISTRATION_DATE", nullable = false, comment = "تاریخ ثبت مشتری")
    private Date registrationDate;

    @Column(name = "CITY_ID", nullable = false, comment = "شناسه شهر مرتبط با رکورد")
    private Long cityId;

    @Basic
    @Column(name = "CUSTOMER_CODE", comment = "کد مشتری")
    private String customerCode;

    @Basic
    @Column(name = "SHAHAB_CODE", comment = "کد شهاب مشتری")
    private String shahabCode;

    @Column(name = "FIRST_NAME", comment = "نام مشتری")
    private String firstName;

    @Column(name = "LAST_NAME", comment = "نام خانوادگی مشتری")
    private String lastName;

    @Column(name = "FATHER_NAME", comment = "نام پدر مشتری")
    private String fatherName;

    @Column(name = "DEATH_DATE", comment = "تاریخ فوت مشتری")
    private Date deathDate;

    @Column(name = "MARITAL_STATUS", comment = "وضعیت تأهل مشتری")
    private MaritalStatus maritalStatus;

    @Column(name = "GENDER", comment = "جنسیت مشتری")
    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddressEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerContactEntity> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SignatureEntity> signatures = new ArrayList<>();


    @Basic
    @Column(name = "CUSTOMER_STATUS", nullable = false, comment = "وضعیت فعال یا غیرفعال مشتری")
    @Enumerated(EnumType.ORDINAL)
    private CustomerStatus customerStatus;

    public void setAddresses(List<CustomerAddressEntity> addresses) {
        this.addresses.clear();
        if (addresses == null) {
            return;
        }
        for (CustomerAddressEntity address : addresses) {
            addAddress(address);
        }
    }

    public void addAddress(CustomerAddressEntity address) {
        if (address != null) {
            address.setCustomer(this);
            this.addresses.add(address);
        }
    }

    public void setContacts(List<CustomerContactEntity> contacts) {
        this.contacts.clear();
        if (contacts == null) {
            return;
        }
        for (CustomerContactEntity contact : contacts) {
            contact.setCustomer(this);
            this.contacts.add(contact);
        }
    }

    public void setSignatures(List<SignatureEntity> signatures) {
        this.signatures.clear();
        if (signatures == null) {
            return;
        }
        for (SignatureEntity signature : signatures) {
            signature.setCustomer(this);
            this.signatures.add(signature);
        }
    }
}
