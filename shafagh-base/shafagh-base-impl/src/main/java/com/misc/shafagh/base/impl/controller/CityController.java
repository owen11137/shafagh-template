package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.base.api.dto.CityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.misc.shafagh.base.impl.service.api.BaseCatalogQueryService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
/** کنترلر REST ماژول BASE برای مدیریت شهر و استان؛ مستندات کامل در Swagger مشترک برنامه. */
@Tag(name="BASE | شهرها", description="مدیریت شهر؛ ثبت، دریافت، فهرست صفحه‌بندی‌شده و ویرایش")
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/city")
public class CityController {
    private final CityService service;
    private final BaseCatalogQueryService queries;
    @Operation(summary="فهرست شهرها", description="فهرست شهرها همراه نام استان؛ رکوردهای parentId=0 استان‌اند و در فهرست و شمارش شهرها نمی‌آیند. پاسخ صفحه‌بندی‌شده است؛ page از صفر، size پیش‌فرض ۲۰ و sort پیش‌فرض id,asc است.")
    @GetMapping public Page<CityDto> findAll(@PageableDefault(size=20, sort="id") Pageable pageable) { return queries.cities(pageable); }
    @Operation(summary="دریافت شهر با شناسه", description="دریافت مشخصات با شناسه مسیر؛ رکورد ناموجود پاسخ 404 دارد.")
    @GetMapping("/{id}") public CityDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @Operation(summary="ثبت شهر جدید", description="id نفرستید؛ شناسه هنگام ثبت تولید می‌شود. نام شهر الزامی است. parentId=0 معرف استان است؛ برای شهر، parentId شناسه والد موجود است.")
    @PostMapping public CityDto save(@RequestBody CityDto dto) { return service.save(dto); }
    @Operation(summary="ویرایش شهر", description="ویرایش رکورد با شناسه مسیر؛ شناسه تغییر نمی‌کند. نام شهر الزامی است. parentId=0 معرف استان است؛ برای شهر، parentId شناسه والد موجود است.")
    @PostMapping("/{id}") public CityDto update(@PathVariable("id") Long id,@RequestBody CityDto dto) { return service.update(id,dto); }
}
