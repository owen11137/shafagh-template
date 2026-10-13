package com.misc.shafagh.cif.impl.entity;

import org.hibernate.annotations.Comment;
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
@Comment("جدول مشتریان حقیقی و حقوقی")
@Entity
@Table(schema = SchemaName.CIF, name = "CUSTOMER")
@SequenceGenerator(name = "SEQ_CUSTOMER_ID", schema = SchemaName.CIF, sequenceName = "SEQ_CUSTOMER_ID", allocationSize = 1)
@Getter
@Setter
public class CustomerEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CUSTOMER_ID")
    @EqualsAndHashCode.Include
    @Comment("شناسه یکتای رکورد مشتریان حقیقی و حقوقی")
    private Long id;

    @Basic
    @Column(name = "IDENTIFIER", nullable = false)
    @Comment("شناسه شناسایی مشتری؛ شماره ملی، شناسه ملی یا شناسه شخص خارجی بر اساس نوع مشتری")
    private String identifier;

    @Column(name = "CUSTOMER_TYPE", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Comment("نوع مشتری؛ حقیقی، حقوقی، حقیقی خارجی یا حقوقی خارجی")
    private CustomerType customerType;

    @Basic
    @Column(name = "REGISTRATION_DATE", nullable = false)
    @Comment("تاریخ ثبت مشتری")
    private Date registrationDate;

    @Column(name = "CITY_ID", nullable = false)
    @Comment("شناسه شهر مرتبط با رکورد")
    private Long cityId;

    @Basic
    @Column(name = "CUSTOMER_CODE")
    @Comment("کد مشتری")
    private String customerCode;

    @Basic
    @Column(name = "SHAHAB_CODE")
    @Comment("کد شهاب مشتری")
    private String shahabCode;

    @Column(name = "FIRST_NAME")
    @Comment("نام مشتری")
    private String firstName;

    @Column(name = "LAST_NAME")
    @Comment("نام خانوادگی مشتری")
    private String lastName;

    @Column(name = "FATHER_NAME")
    @Comment("نام پدر مشتری")
    private String fatherName;

    @Column(name = "DEATH_DATE")
    @Comment("تاریخ فوت مشتری")
    private Date deathDate;

    @Column(name = "MARITAL_STATUS")
    @Comment("وضعیت تأهل مشتری")
    private MaritalStatus maritalStatus;

    @Column(name = "GENDER")
    @Enumerated(EnumType.ORDINAL)
    @Comment("جنسیت مشتری")
    private Gender gender;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddressEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerContactEntity> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SignatureEntity> signatures = new ArrayList<>();


    @Basic
    @Column(name = "CUSTOMER_STATUS", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Comment("وضعیت فعال یا غیرفعال مشتری")
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