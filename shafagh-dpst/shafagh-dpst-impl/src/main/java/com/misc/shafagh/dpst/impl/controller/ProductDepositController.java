package com.misc.shafagh.dpst.impl.controller;

import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto;
import com.misc.shafagh.dpst.impl.service.api.ProductDepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** کنترلر DPST برای ثبت، دریافت، فهرست و ویرایش محصول سپرده صندوق فعال. */
@Tag(name="DPST | محصولات سپرده", description="ثبت، دریافت، فهرست صفحه‌بندی‌شده و ویرایش محصول سپرده در صندوق فعال")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dpst/products")
public class ProductDepositController {
    private final ProductDepositService service;

    @Operation(summary="ثبت محصول سپرده", description="کد یکتا، depositType و status الزامی‌اند؛ عنوان، کد و توضیحات حداکثر ۲۵۵ کاراکتر. تاریخ پایان نباید قبل از شروع باشد. شناسه، صندوق و اطلاعات ثبت توسط برنامه تعیین می‌شوند.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDepositResponseDto create(@Valid @RequestBody ProductDepositReqDTO request) { return service.create(request); }

    @Operation(summary="دریافت محصول سپرده", description="دریافت مشخصات محصول با شناسه؛ محصول ناموجود یا متعلق به صندوق دیگر 404 دارد.")
    @GetMapping("/{id}")
    public ProductDepositResponseDto getById(@PathVariable("id") Long id) { return service.getById(id); }

    @Operation(summary="فهرست محصولات سپرده", description="فهرست صفحه‌بندی‌شده محصولات صندوق فعال؛ page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است. پاسخ شامل content و اطلاعات تعداد رکوردها و صفحات است.")
    @GetMapping
    public Page<ProductDepositResponseDto> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) { return service.findAll(pageable); }

    @Operation(summary="ویرایش محصول سپرده", description="ویرایش کامل محصول با شناسه مسیر در صندوق فعال؛ شناسه، مالکیت و زمان ثبت حفظ می‌شوند. کد محصول دارای حساب قابل تغییر نیست.")
    @PostMapping("/{id}")
    public ProductDepositResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductDepositReqDTO request) { return service.update(id, request); }

}
