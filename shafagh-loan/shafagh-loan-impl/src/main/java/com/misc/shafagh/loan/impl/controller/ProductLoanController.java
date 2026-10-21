package com.misc.shafagh.loan.impl.controller;

import com.misc.shafagh.loan.impl.dto.request.ProductLoanReqDTO;
import com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto;
import com.misc.shafagh.loan.impl.service.api.ProductLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** کنترلر LOAN برای ثبت، دریافت، فهرست و ویرایش محصول تسهیلات صندوق فعال. */
@Tag(name="LOAN | محصولات تسهیلات", description="ثبت، دریافت، فهرست صفحه‌بندی‌شده و ویرایش محصول تسهیلات در صندوق فعال")
@RestController
@RequiredArgsConstructor
@RequestMapping("/loan/products")
public class ProductLoanController {
    private final ProductLoanService service;

    @Operation(summary="ثبت محصول تسهیلات", description="کد یکتا با حداکثر ۲۰ کاراکتر، productType و earlySettlementAllowed الزامی‌اند. مقادیر مالی و تعداد اقساط نامنفی و بازه‌های حداقل و حداکثر معتبر باشند. شناسه، صندوق و اطلاعات ثبت توسط برنامه تعیین می‌شوند.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductLoanResponseDto create(@Valid @RequestBody ProductLoanReqDTO request) { return service.create(request); }

    @Operation(summary="دریافت محصول تسهیلات", description="دریافت مشخصات محصول با شناسه؛ محصول ناموجود یا متعلق به صندوق دیگر 404 دارد.")
    @GetMapping("/{id}")
    public ProductLoanResponseDto getById(@PathVariable("id") Long id) { return service.getById(id); }

    @Operation(summary="فهرست محصولات تسهیلات", description="فهرست صفحه‌بندی‌شده محصولات صندوق فعال؛ page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است. پاسخ شامل content و اطلاعات تعداد رکوردها و صفحات است.")
    @GetMapping
    public Page<ProductLoanResponseDto> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) { return service.findAll(pageable); }

    @Operation(summary="ویرایش محصول تسهیلات", description="ویرایش کامل محصول با شناسه مسیر در صندوق فعال؛ شناسه، مالکیت و زمان ثبت حفظ می‌شوند. بازه‌های مالی، اقساط و تاریخ‌ها باید معتبر باشند.")
    @PostMapping("/{id}")
    public ProductLoanResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductLoanReqDTO request) { return service.update(id, request); }

}
