package com.misc.shafagh.loan.impl.service.port;

import com.misc.shafagh.loan.impl.dto.request.ProductLoanReqDTO;
import com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** عملیات محصول تسهیلات در صندوق فعال برنامه. */
public interface ProductLoanService {
    ProductLoanResponseDto create(ProductLoanReqDTO request);
    ProductLoanResponseDto getById(Long id);
    Page<ProductLoanResponseDto> findAll(Pageable pageable);
    ProductLoanResponseDto update(Long id, ProductLoanReqDTO request);
    void delete(Long id);
}
