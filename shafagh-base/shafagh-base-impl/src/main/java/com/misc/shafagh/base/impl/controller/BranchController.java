package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.BranchService;
import com.misc.shafagh.base.api.dto.BranchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.misc.shafagh.base.impl.service.api.BaseCatalogQueryService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
/** کنترلر REST ماژول BASE برای مدیریت شعبه؛ مستندات کامل در Swagger مشترک برنامه. */
@Tag(name="BASE | شعبه‌ها", description="مدیریت شعبه؛ ثبت، دریافت، فهرست صفحه‌بندی‌شده و ویرایش")
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/branch")
public class BranchController {
    private final BranchService service;
    private final BaseCatalogQueryService queries;
    @Operation(summary="فهرست شعبه‌ها", description="فهرست شعبه‌های صندوق جاری. پاسخ صفحه‌بندی‌شده است؛ page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است.")
    @GetMapping public Page<BranchDto> findAll(@PageableDefault(size=20, sort="id") Pageable pageable) { return queries.branches(pageable); }
    @Operation(summary="دریافت شعبه با شناسه", description="دریافت مشخصات با شناسه مسیر؛ رکورد ناموجود پاسخ 404 دارد.")
    @GetMapping("/{id}") public BranchDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @Operation(summary="ثبت شعبه جدید", description="id نفرستید؛ شناسه هنگام ثبت تولید می‌شود. کد و نام شعبه الزامی‌اند. fundId اختیاری است؛ فقط صندوق جاری مجاز است و باید موجود باشد. کد شعبه در هر صندوق یکتا است.")
    @PostMapping public BranchDto save(@RequestBody BranchDto dto) { return service.save(dto); }
    @Operation(summary="ویرایش شعبه", description="ویرایش رکورد با شناسه مسیر؛ شناسه تغییر نمی‌کند. کد و نام شعبه الزامی‌اند. fundId اختیاری است؛ فقط صندوق جاری مجاز است و باید موجود باشد. کد شعبه در هر صندوق یکتا است.")
    @PostMapping("/{id}") public BranchDto update(@PathVariable("id") Long id,@RequestBody BranchDto dto) { return service.update(id,dto); }
}
