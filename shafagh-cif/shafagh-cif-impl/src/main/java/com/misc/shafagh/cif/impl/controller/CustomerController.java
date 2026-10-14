package com.misc.shafagh.cif.impl.controller;

import com.misc.shafagh.cif.impl.dto.request.CustomerFilterDTO;
import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerDto;
import com.misc.shafagh.cif.impl.service.api.CustomerLegalService;
import com.misc.shafagh.cif.impl.service.api.CustomerRealService;
import lombok.RequiredArgsConstructor;
import com.misc.shafagh.cif.api.CustomerLookup;
import com.misc.shafagh.cif.api.dto.CustomerExistenceStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

/** کنترلر REST ماژول CIF برای مدیریت مشتریان حقیقی و حقوقی؛ مستندات کامل در Swagger مشترک برنامه. */
@Tag(name="CIF | مشتریان", description="ثبت، ویرایش، جست‌وجو و بررسی وجود مشتری حقیقی و حقوقی در صندوق جاری")
@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerRealService customerRealService;
    private final CustomerLegalService customerLegalService;
    private final CustomerLookup customerLookup;

    @Operation(summary="بررسی وجود مشتری در صندوق جاری", description="کد ملی یا شماره ثبت شرکت را در identifier ارسال کنید. شرط FUND_ID از tenant جاری گرفته می‌شود؛ وجود مشتری در صندوق دیگر کافی نیست. خروجی EXIST یا NOT_EXIST است و به نوع یا وضعیت مشتری محدود نمی‌شود.")
    @PostMapping(value="/exists", produces=org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public CustomerExistenceStatus existsByIdentifier(
            @Parameter(description="کد ملی یا شماره ثبت شرکت؛ فقط ارقام ۰ تا ۹، حداکثر ۲۰ رقم، با حفظ صفر ابتدایی", required=true)
            @RequestParam("identifier") @NotBlank @Size(max=20) @Pattern(regexp="[0-9]+") String identifier) {
        return customerLookup.existsByIdentifier(identifier);
    }

    @Operation(summary="ثبت شخص حقیقی", description="id نفرستید. شناسه شناسایی در هر صندوق یکتا، نوع شخص، تاریخ ثبت، شهر موجود، حداقل یک آدرس و یک راه ارتباطی لازم است. صندوق جاری و وضعیت ACTIVE توسط سرویس تعیین می‌شوند. نوع REAL یا FOREIGN_REAL؛ نام، نام خانوادگی، نام پدر، جنسیت و وضعیت تأهل الزامی‌اند.")
    @PostMapping("/real/save")
    public CustomerDto saveReal(@RequestBody CustomerReqDTO request) {
        return customerRealService.save(request);
    }

    @Operation(summary="ویرایش شخص حقیقی", description="id مشتری در بدنه الزامی است. اطلاعات کامل ثبت را ارسال کنید؛ customerCode حفظ می‌شود. آدرس و تماس موجود باید متعلق به همین مشتری باشند؛ اعضای حذف‌شده از فهرست حذف می‌شوند. شناسه تکراری در صندوق جاری پاسخ 409 دارد.")
    @PostMapping("/real/update")
    public CustomerDto updateReal(@RequestBody CustomerReqDTO request) {
        return customerRealService.update(request);
    }

    @Operation(summary="دریافت شخص حقیقی با شناسه", description="مشخصات مشتری از نوع مجاز همین سرویس در صندوق جاری، همراه شهر، آدرس‌ها، تماس‌ها و امضاها.")
    @PostMapping("/real/getById/{id}")
    public CustomerDto getByIdReal(@PathVariable("id") Long id) {
        return customerRealService.getById(id);
    }

    @Operation(summary="جست‌وجوی اشخاص حقیقی", description="فیلترهای بدنه اختیاری‌اند؛ فقط انواع مجاز همین سرویس در صندوق جاری بررسی می‌شوند. page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است. خروجی شامل content و اطلاعات صفحات است.")
    @PostMapping("/real/filter")
    public Page<CustomerDto> searchReal(@RequestBody CustomerFilterDTO filter, @PageableDefault(size=20, sort="id") Pageable pageable) {
        return customerRealService.search(filter, pageable);
    }

    @Operation(summary="ثبت شخص حقوقی", description="id نفرستید. شناسه شناسایی در هر صندوق یکتا، نوع شخص، تاریخ ثبت، شهر موجود، حداقل یک آدرس و یک راه ارتباطی لازم است. صندوق جاری و وضعیت ACTIVE توسط سرویس تعیین می‌شوند. نوع LEGAL یا FOREIGN_LEGAL؛ هنگام ثبت نوع آدرس WORK_PLACE تعیین می‌شود.")
    @PostMapping("/legal/save")
    public CustomerDto saveLegal(@RequestBody CustomerReqDTO request) {
        return customerLegalService.save(request);
    }

    @Operation(summary="ویرایش شخص حقوقی", description="id مشتری در بدنه الزامی است. اطلاعات کامل ثبت را ارسال کنید؛ customerCode حفظ می‌شود. آدرس و تماس موجود باید متعلق به همین مشتری باشند؛ اعضای حذف‌شده از فهرست حذف می‌شوند. شناسه تکراری در صندوق جاری پاسخ 409 دارد.")
    @PostMapping("/legal/update")
    public CustomerDto updateLegal(@RequestBody CustomerReqDTO request) {
        return customerLegalService.update(request);
    }

    @Operation(summary="دریافت شخص حقوقی با شناسه", description="مشخصات مشتری از نوع مجاز همین سرویس در صندوق جاری، همراه شهر، آدرس‌ها، تماس‌ها و امضاها.")
    @PostMapping("/legal/getById/{id}")
    public CustomerDto getByIdLegal(@PathVariable("id") Long id) {
        return customerLegalService.getById(id);
    }

    @Operation(summary="جست‌وجوی اشخاص حقوقی", description="فیلترهای بدنه اختیاری‌اند؛ فقط انواع مجاز همین سرویس در صندوق جاری بررسی می‌شوند. page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است. خروجی شامل content و اطلاعات صفحات است.")
    @PostMapping("/legal/filter")
    public Page<CustomerDto> searchLegal(@RequestBody CustomerFilterDTO filter, @PageableDefault(size=20, sort="id") Pageable pageable) {
        return customerLegalService.search(filter, pageable);
    }

    @Operation(summary="دریافت مشتری با شناسه شناسایی", description="identifier را در مسیر ارسال کنید؛ جست‌وجو در صندوق جاری انجام می‌شود و به نوع شخص محدود نیست. اگر مشتری وجود نداشته باشد، بدنه پاسخ خالی است.")
    @PostMapping("/{identifier}")
    public CustomerDto getByIdentifier(@PathVariable("identifier") String identifier) {
        return customerLegalService.getByIdentifier(identifier);
    }
}
