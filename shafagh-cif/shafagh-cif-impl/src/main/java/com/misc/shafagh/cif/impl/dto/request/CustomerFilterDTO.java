package com.misc.shafagh.cif.impl.dto.request;

import com.misc.shafagh.cif.impl.entity.enums.Gender;
import com.misc.shafagh.cif.impl.entity.enums.MaritalStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class CustomerFilterDTO {
    private String identifier;
    private CustomerType customerType;
    private Date registrationDate;
    private Long cityId;
    private String customerCode;
    private String shahabCode;
    private String firstName;
    private String lastName;
    private String fatherName;
    private Date deathDate;
    private MaritalStatus maritalStatus;
    private Gender gender;
    private CustomerStatus customerStatus;
}
