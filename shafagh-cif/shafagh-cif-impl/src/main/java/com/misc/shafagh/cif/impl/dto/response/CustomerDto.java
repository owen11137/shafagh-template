package com.misc.shafagh.cif.impl.dto.response;


import com.misc.shafagh.cif.impl.entity.enums.Gender;
import com.misc.shafagh.cif.impl.entity.enums.MaritalStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerStatus;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CustomerDto {

    private Long id;
    private String identifier;
    private CustomerType customerType;
    private Date registrationDate;
    private CityDto city;
    private String customerCode;
    private String shahabCode;
    private String firstName;
    private String lastName;
    private String fatherName;
    private Date deathDate;
    private MaritalStatus maritalStatus;
    private Gender gender;
    private CustomerStatus customerStatus;
    private List<CustomerAddressDto> addresses = new ArrayList<>();
    private List<CustomerContactDto> contacts = new ArrayList<>();
    private List<SignatureDto> signatures = new ArrayList<>();
}
