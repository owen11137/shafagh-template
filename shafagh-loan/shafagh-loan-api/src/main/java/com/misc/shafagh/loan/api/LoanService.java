package com.misc.shafagh.loan.api;

import com.misc.shafagh.loan.api.dto.LoanDto;
import com.misc.shafagh.loan.api.dto.RegisterLoanRequest;

/** سرویس expose شده‌ی ماژول تسهیلات. */
public interface LoanService {

    LoanDto register(RegisterLoanRequest request);

    LoanDto getById(Long id);
}
