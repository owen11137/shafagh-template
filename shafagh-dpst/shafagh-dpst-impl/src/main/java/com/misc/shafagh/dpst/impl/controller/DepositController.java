package com.misc.shafagh.dpst.impl.controller;
import com.misc.shafagh.dpst.impl.dto.request.OpenDepositRequest;
import com.misc.shafagh.dpst.impl.dto.response.OpenDepositResponse;
import com.misc.shafagh.dpst.impl.service.DepositOpeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
/** درگاه افتتاح حساب سپرده برای مشتری حقیقی صندوق فعال. */
@RestController
@RequestMapping("/dpst/deposits")
@RequiredArgsConstructor
public class DepositController {
    private final DepositOpeningService service;
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public OpenDepositResponse open(@Valid @RequestBody OpenDepositRequest request) {
        return service.open(request);
    }
}
