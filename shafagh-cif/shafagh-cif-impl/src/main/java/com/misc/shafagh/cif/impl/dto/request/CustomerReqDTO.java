package com.misc.shafagh.cif.impl.dto.request;


import com.misc.shafagh.core.entity.enums.Gender;
import com.misc.shafagh.core.entity.enums.MaritalStatus;
import com.misc.shafagh.core.entity.enums.CustomerStatus;
import com.misc.shafagh.core.entity.enums.CustomerType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
public class CustomerReqDTO {
   //common
    private Long id;
    private Long fundId;
    private String identifier;
    private CustomerType customerType;
    private Date registrationDate;
    private String customerCode;
    private Long cityId;
    private Date deathDate;
    private CustomerStatus customerStatus;
   private List<CustomerAddressReqDTO> addresses = new ArrayList<>();
   private List<CustomerContactReqDTO> contacts = new ArrayList<>();
    private List<SignatureReqDTO> signatures = new ArrayList<>();

   //real
    private String shahabCode;
    private String firstName;
    private String lastName;
    private String fatherName;
    private MaritalStatus maritalStatus;
    private Gender gender;

}
