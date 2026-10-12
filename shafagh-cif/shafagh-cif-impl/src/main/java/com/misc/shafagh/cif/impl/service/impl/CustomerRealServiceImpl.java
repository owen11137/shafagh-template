package com.misc.shafagh.cif.impl.service.impl;

import com.misc.shafagh.cif.impl.dto.mapper.CustomerMapper;
import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import com.misc.shafagh.cif.impl.entity.validator.CustomerValidator;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.cif.impl.repository.CustomerRepository;
import com.misc.shafagh.cif.impl.service.port.CustomerRealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
@Transactional
public class CustomerRealServiceImpl extends AbstractCustomerService implements CustomerRealService {

    private static final Set<CustomerType> ALLOWED_TYPES =
            EnumSet.of(CustomerType.REAL, CustomerType.FOREIGN_REAL);

    public CustomerRealServiceImpl(CustomerRepository personRepo,
                                   CityService cityService,
                                   CustomerMapper customerMapper,
                                   CustomerValidator customerValidator,
                                   CustomerSignatureHelper signatureHelper) {
        super(personRepo, cityService, customerMapper, customerValidator, signatureHelper);
    }

    @Override
    protected Set<CustomerType> allowedTypes() {
        return ALLOWED_TYPES;
    }

    @Override
    protected String personLabel() {
        return "حقیقی";
    }

    @Override
    protected void validateSpecific(CustomerReqDTO reqDTO) {
        customerValidator.validateReal(reqDTO);
    }
}
