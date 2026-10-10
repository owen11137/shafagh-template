package com.misc.shafagh.dpst.api;

import com.misc.shafagh.dpst.api.dto.DepositDto;
import com.misc.shafagh.dpst.api.dto.OpenDepositRequest;
import java.math.BigDecimal;

/** سرویس expose شده‌ی ماژول سپرده. */
public interface DepositService {

    DepositDto open(OpenDepositRequest request);

    DepositDto getById(Long id);

    /** مبلغ را روی سپرده بلوکه می‌کند. در تراکنشِ فراخواننده شرکت می‌کند. */
    DepositDto blockAmount(Long depositId, BigDecimal amount);
}
