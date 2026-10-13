package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.FundService;
import com.misc.shafagh.base.api.dto.FundDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.misc.shafagh.base.impl.service.api.BaseCatalogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/** کنترلر REST ماژول BASE برای مدیریت صندوق؛ مستندات کامل در Swagger مشترک برنامه. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/fund")
public class FundController {
    private final FundService service;
    private final BaseCatalogQueryService queries;
    @GetMapping public Page<FundDto> findAll(@PageableDefault(size=20, sort="id") Pageable pageable) { return queries.funds(pageable); }
    @GetMapping("/{id}") public FundDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @PostMapping public FundDto save(@RequestBody FundDto dto) { return service.save(dto); }
    @PutMapping("/{id}") public FundDto update(@PathVariable("id") Long id,@RequestBody FundDto dto) { return service.update(id,dto); }
}
