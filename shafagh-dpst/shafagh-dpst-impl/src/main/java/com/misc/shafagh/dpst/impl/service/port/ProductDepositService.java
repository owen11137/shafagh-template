package com.misc.shafagh.dpst.impl.service.port;

import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** عملیات محصول سپرده در صندوق فعال برنامه. */
public interface ProductDepositService {
    ProductDepositResponseDto create(ProductDepositReqDTO request);
    ProductDepositResponseDto getById(Long id);
    Page<ProductDepositResponseDto> findAll(Pageable pageable);
    ProductDepositResponseDto update(Long id, ProductDepositReqDTO request);
    void delete(Long id);
}
