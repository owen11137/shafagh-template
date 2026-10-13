package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.base.api.dto.CityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.misc.shafagh.base.impl.service.impl.BaseCatalogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/** کنترلر REST ماژول BASE برای مدیریت شهر و استان؛ مستندات کامل در Swagger مشترک برنامه. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/city")
public class CityController {
    private final CityService service;
    private final BaseCatalogQueryService queries;
    @GetMapping public Page<CityDto> findAll(@PageableDefault(size=20, sort="id") Pageable pageable) { return queries.cities(pageable); }
    @GetMapping("/{id}") public CityDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @PostMapping public CityDto save(@RequestBody CityDto dto) { return service.save(dto); }
    @PutMapping("/{id}") public CityDto update(@PathVariable("id") Long id,@RequestBody CityDto dto) { return service.update(id,dto); }
}
