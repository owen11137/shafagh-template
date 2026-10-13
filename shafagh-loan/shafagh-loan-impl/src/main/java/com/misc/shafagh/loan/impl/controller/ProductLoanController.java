package com.misc.shafagh.loan.impl.controller;

import com.misc.shafagh.loan.impl.dto.request.ProductLoanReqDTO;
import com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto;
import com.misc.shafagh.loan.impl.service.port.ProductLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** کنترلر LOAN برای ثبت، دریافت، فهرست، ویرایش و حذف محصول تسهیلات صندوق فعال. */
@Tag(name="LOAN | محصولات تسهیلات", description="ثبت، دریافت، فهرست صفحه‌بندی‌شده، ویرایش و حذف محصول تسهیلات در صندوق فعال")
@RestController
@RequiredArgsConstructor
@RequestMapping("/loan/products")
public class ProductLoanController {
    private final ProductLoanService service;

    @Operation(summary="ثبت محصول تسهیلات", description="کد یکتا با حداکثر ۲۰ کاراکتر، productType و earlySettlementAllowed الزامی‌اند. مقادیر مالی و تعداد اقساط نامنفی و بازه‌های حداقل و حداکثر معتبر باشند. شناسه، صندوق و اطلاعات ثبت توسط برنامه تعیین می‌شوند.")
    @ApiResponses({
        @ApiResponse(responseCode="201", description="عملیات موفق"),
        @ApiResponse(responseCode="400", description="ورودی نامعتبر یا بازه نامعتبر", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="409", description="کد محصول تکراری است؛ یکتایی کد در کل اسکیما برقرار است", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductLoanResponseDto create(@Valid @RequestBody ProductLoanReqDTO request) { return service.create(request); }

    @Operation(summary="دریافت محصول تسهیلات", description="دریافت مشخصات محصول با شناسه؛ محصول ناموجود یا متعلق به صندوق دیگر 404 دارد.")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="عملیات موفق"),
        @ApiResponse(responseCode="400", description="شناسه باید مثبت باشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="404", description="محصول در صندوق فعال پیدا نشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ProductLoanResponseDto getById(@PathVariable("id") Long id) { return service.getById(id); }

    @Operation(summary="فهرست محصولات تسهیلات", description="فهرست صفحه‌بندی‌شده محصولات صندوق فعال؛ page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است. پاسخ شامل content و اطلاعات تعداد رکوردها و صفحات است.")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="عملیات موفق")
    })
    @GetMapping
    public Page<ProductLoanResponseDto> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) { return service.findAll(pageable); }

    @Operation(summary="ویرایش محصول تسهیلات", description="ویرایش کامل محصول با شناسه مسیر در صندوق فعال؛ شناسه، مالکیت و زمان ثبت حفظ می‌شوند. بازه‌های مالی، اقساط و تاریخ‌ها باید معتبر باشند.")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="عملیات موفق"),
        @ApiResponse(responseCode="400", description="ورودی یا بازه نامعتبر", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="404", description="محصول در صندوق فعال پیدا نشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="409", description="کد تکراری یا تعارض هم‌زمانی", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ProductLoanResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductLoanReqDTO request) { return service.update(id, request); }

    @Operation(summary="حذف محصول تسهیلات", description="حذف محصول صندوق فعال؛ پاسخ موفق بدون بدنه است.")
    @ApiResponses({
        @ApiResponse(responseCode="204", description="عملیات موفق", content=@Content),
        @ApiResponse(responseCode="400", description="شناسه باید مثبت باشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="404", description="محصول در صندوق فعال پیدا نشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) { service.delete(id); }
}
