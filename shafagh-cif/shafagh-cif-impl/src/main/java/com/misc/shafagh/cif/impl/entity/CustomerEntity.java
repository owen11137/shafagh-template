package com.misc.shafagh.cif.impl.entity;

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
@Table(schema = CifSchema.NAME, name = "CUSTOMER")
@SequenceGenerator(name = "CUSTOMER_ID_SEQ", schema = CifSchema.NAME, sequenceName = "CUSTOMER_ID_SEQ", allocationSize = 1)
@Getter
@Setter
public class CustomerEntity extends TenantAwareEntity {

    @Id
    @Column(name = "ID", columnDefinition = "number(19,0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CUSTOMER_ID_SEQ")
    @EqualsAndHashCode.Include
    private Long id;

    @Basic
    @Column(name = "IDENTIFIER", nullable = false)
    private String identifier;

    @Column(name = "CUSTOMER_TYPE", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private CustomerType customerType;

    @Basic
    @Column(name = "REGISTRATION_DATE", nullable = false)
    private Date registrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="CITY_ID", referencedColumnName = "ID", nullable = false, foreignKey = @ForeignKey(name = "CUSTOMER_FK_CITY"))
    private CityEntity city;

    @Basic
    @Column(name = "CUSTOMER_CODE")
    private String customerCode;

    @Basic
    @Column(name = "SHAHAB_CODE")
    private String shahabCode;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "FATHER_NAME")
    private String fatherName;

    @Column(name = "DEATH_DATE")
    private Date deathDate;

    @Column(name = "MARITAL_STATUS")
    private MaritalStatus maritalStatus;

    @Column(name = "GENDER")
    @Enumerated(EnumType.ORDINAL)
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