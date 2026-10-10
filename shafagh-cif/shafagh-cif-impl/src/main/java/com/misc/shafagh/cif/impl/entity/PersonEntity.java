package com.misc.shafagh.cif.impl.entity;

import com.misc.shafagh.cif.impl.CifSchema;
import com.misc.shafagh.cif.api.dto.PersonStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(schema = CifSchema.NAME, name = "SHFQ_CIF_PERSON")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shfq_cif_person_gen")
    @SequenceGenerator(name = "shfq_cif_person_gen", schema = CifSchema.NAME, sequenceName = "SHFQ_CIF_PERSON_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NATIONAL_CODE", nullable = false, unique = true, length = 20)
    private String nationalCode;

    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private PersonStatus status;

    protected PersonEntity() {
    }

    public PersonEntity(String nationalCode, String firstName, String lastName) {
        this.nationalCode = nationalCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = PersonStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public String getNationalCode() { return nationalCode; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public PersonStatus getStatus() { return status; }
}
