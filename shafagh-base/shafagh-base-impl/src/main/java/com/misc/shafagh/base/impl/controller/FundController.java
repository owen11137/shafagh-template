package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.FundService;
import com.misc.shafagh.base.api.dto.FundDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.misc.shafagh.base.impl.service.api.BaseCatalogQueryService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
/** کنترلر REST ماژول BASE برای مدیریت صندوق؛ مستندات کامل در Swagger مشترک برنامه. */
@Tag(name="BASE | صندوق‌ها", description="مدیریت صندوق؛ ثبت، دریافت، فهرست صفحه‌بندی‌شده و ویرایش")
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/fund")
public class FundController {
    private final FundService service;
    private final BaseCatalogQueryService queries;
    @Operation(summary="فهرست صندوق‌ها", description="فهرست صندوق‌ها. پاسخ صفحه‌بندی‌شده است؛ page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است.")
    @GetMapping public Page<FundDto> findAll(@PageableDefault(size=20, sort="id") Pageable pageable) { return queries.funds(pageable); }
    @Operation(summary="دریافت صندوق با شناسه", description="دریافت مشخصات با شناسه مسیر؛ رکورد ناموجود پاسخ 404 دارد.")
    @GetMapping("/{id}") public FundDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @Operation(summary="ثبت صندوق جدید", description="id نفرستید؛ شناسه هنگام ثبت تولید می‌شود. کد و نام صندوق الزامی‌اند. ownerId شناسه مشتری موجود در CIF و صندوق جاری برنامه است.")
    @PostMapping public FundDto save(@RequestBody FundDto dto) { return service.save(dto); }
    @Operation(summary="ویرایش صندوق", description="ویرایش رکورد با شناسه مسیر؛ شناسه تغییر نمی‌کند. کد و نام صندوق الزامی‌اند. ownerId شناسه مشتری موجود در CIF و صندوق جاری برنامه است.")
    @PostMapping("/{id}") public FundDto update(@PathVariable("id") Long id,@RequestBody FundDto dto) { return service.update(id,dto); }
}
