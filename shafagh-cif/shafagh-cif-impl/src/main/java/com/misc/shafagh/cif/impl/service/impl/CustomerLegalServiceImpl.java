package com.misc.shafagh.cif.impl.service.impl;

import com.misc.shafagh.cif.impl.dto.mapper.CustomerMapper;
import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerDto;
import com.misc.shafagh.cif.impl.entity.enums.AddressType;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import com.misc.shafagh.cif.impl.entity.validator.CustomerValidator;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.cif.impl.repository.CustomerRepository;
import com.misc.shafagh.cif.impl.service.api.CustomerLegalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
@Transactional("cifTransactionManager")
public class CustomerLegalServiceImpl extends AbstractCustomerService implements CustomerLegalService {

    private static final Set<CustomerType> ALLOWED_TYPES =
            EnumSet.of(CustomerType.LEGAL, CustomerType.FOREIGN_LEGAL);

    public CustomerLegalServiceImpl(CustomerRepository personRepo,
                                    CityService cityService,
                                    CustomerMapper customerMapper,
                                    CustomerValidator customerValidator,
                                    CustomerSignatureHelper signatureHelper, com.misc.shafagh.common.context.CurrentUserContext currentUserContext) {
        super(personRepo, cityService, customerMapper, customerValidator, signatureHelper, currentUserContext);
    }

    @Override
    protected Set<CustomerType> allowedTypes() {
        return ALLOWED_TYPES;
    }

    @Override
    protected String personLabel() {
        return "حقوقی";
    }

    @Override
    protected void validateSpecific(CustomerReqDTO reqDTO) {
        customerValidator.validateLegal(reqDTO);
    }

    @Override
    protected void beforeSave(CustomerReqDTO reqDTO) {
        forEachSafe(reqDTO.getAddresses(), a -> a.setAddressType(AddressType.WORK_PLACE));
    }

    @Override
    @Transactional(transactionManager = "cifTransactionManager", readOnly = true)
    public CustomerDto getByIdentifier(String identifier) {
        return customerMapper.toDto(personRepo.getFirstByIdentifier(identifier));
    }
}
