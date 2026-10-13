package com.misc.shafagh.dpst.impl.controller;

import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto;
import com.misc.shafagh.dpst.impl.service.port.ProductDepositService;
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

/** کنترلر DPST برای ثبت، دریافت، فهرست، ویرایش و حذف محصول سپرده صندوق فعال. */
@Tag(name="DPST | محصولات سپرده", description="ثبت، دریافت، فهرست صفحه‌بندی‌شده، ویرایش و حذف محصول سپرده در صندوق فعال")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dpst/products")
public class ProductDepositController {
    private final ProductDepositService service;

    @Operation(summary="ثبت محصول سپرده", description="کد یکتا، depositType و status الزامی‌اند؛ عنوان، کد و توضیحات حداکثر ۲۵۵ کاراکتر. تاریخ پایان نباید قبل از شروع باشد. شناسه، صندوق و اطلاعات ثبت توسط برنامه تعیین می‌شوند.")
    @ApiResponses({
        @ApiResponse(responseCode="201", description="عملیات موفق"),
        @ApiResponse(responseCode="400", description="ورودی نامعتبر یا بازه نامعتبر", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="409", description="کد محصول تکراری است؛ یکتایی کد در کل اسکیما برقرار است", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDepositResponseDto create(@Valid @RequestBody ProductDepositReqDTO request) { return service.create(request); }

    @Operation(summary="دریافت محصول سپرده", description="دریافت مشخصات محصول با شناسه؛ محصول ناموجود یا متعلق به صندوق دیگر 404 دارد.")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="عملیات موفق"),
        @ApiResponse(responseCode="400", description="شناسه باید مثبت باشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="404", description="محصول در صندوق فعال پیدا نشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ProductDepositResponseDto getById(@PathVariable("id") Long id) { return service.getById(id); }

    @Operation(summary="فهرست محصولات سپرده", description="فهرست صفحه‌بندی‌شده محصولات صندوق فعال؛ page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است. پاسخ شامل content و اطلاعات تعداد رکوردها و صفحات است.")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="عملیات موفق")
    })
    @GetMapping
    public Page<ProductDepositResponseDto> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) { return service.findAll(pageable); }

    @Operation(summary="ویرایش محصول سپرده", description="ویرایش کامل محصول با شناسه مسیر در صندوق فعال؛ شناسه، مالکیت و زمان ثبت حفظ می‌شوند. کد محصول دارای حساب قابل تغییر نیست.")
    @ApiResponses({
        @ApiResponse(responseCode="200", description="عملیات موفق"),
        @ApiResponse(responseCode="400", description="ورودی یا بازه نامعتبر", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="404", description="محصول در صندوق فعال پیدا نشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="409", description="کد تکراری یا تعارض هم‌زمانی؛ تغییر کد محصول دارای حساب مجاز نیست", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public ProductDepositResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductDepositReqDTO request) { return service.update(id, request); }

    @Operation(summary="حذف محصول سپرده", description="حذف محصول صندوق فعال؛ پاسخ موفق بدون بدنه است. محصول دارای حساب قابل حذف نیست؛ آن را غیرفعال کنید.")
    @ApiResponses({
        @ApiResponse(responseCode="204", description="عملیات موفق", content=@Content),
        @ApiResponse(responseCode="400", description="شناسه باید مثبت باشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="404", description="محصول در صندوق فعال پیدا نشد", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class))),
        @ApiResponse(responseCode="409", description="محصول دارای حساب است؛ حذف مجاز نیست", content=@Content(mediaType="application/problem+json", schema=@Schema(implementation=org.springframework.http.ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) { service.delete(id); }
}
