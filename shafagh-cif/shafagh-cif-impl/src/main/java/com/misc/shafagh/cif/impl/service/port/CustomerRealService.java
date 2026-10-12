package com.misc.shafagh.cif.impl.service.port;


import com.misc.shafagh.cif.impl.dto.request.CustomerFilterDTO;
import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerRealService {

    Page<CustomerDto> findAll(Pageable pageable);

    CustomerDto save(CustomerReqDTO reqDTO);

    Page<CustomerDto> search(CustomerFilterDTO filter, Pageable pageable);

    CustomerDto update(CustomerReqDTO reqDTO);

    CustomerDto getById(long id);

    void delete(long id);
}
