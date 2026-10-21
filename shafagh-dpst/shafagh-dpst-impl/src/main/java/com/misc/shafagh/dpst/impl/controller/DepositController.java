package com.misc.shafagh.dpst.impl.controller;
import com.misc.shafagh.dpst.impl.dto.request.OpenDepositRequest;
import com.misc.shafagh.dpst.impl.dto.response.OpenDepositResponse;
import com.misc.shafagh.dpst.impl.service.api.DepositOpeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
/** درگاه افتتاح حساب سپرده برای مشتری حقیقی صندوق فعال. */
@Tag(name="DPST | افتتاح حساب", description="افتتاح حساب سپرده و ثبت مالک برای مشتری حقیقی صندوق جاری")
@RestController
@RequestMapping("/dpst/deposits")
@RequiredArgsConstructor
public class DepositController {
    private final DepositOpeningService service;
    @Operation(summary="افتتاح حساب سپرده", description="شماره ملی ده رقمی مشتری حقیقی فعال و productCode محصول فعال در صندوق جاری را ارسال کنید. حساب با مانده صفر و وضعیت OPN همراه مالک، اتمیک ثبت می‌شود. شماره حساب داخلی ۲۰ رقمی تولید می‌شود؛ شبا نیست. description توضیحات اختیاری تا ۲۵۵ کاراکتر است.")
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public OpenDepositResponse open(@Valid @RequestBody OpenDepositRequest request) {
        return service.open(request);
    }
}
