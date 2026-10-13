package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.BranchService;
import com.misc.shafagh.base.api.dto.BranchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.misc.shafagh.base.impl.service.impl.BaseCatalogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/** کنترلر REST ماژول BASE برای مدیریت شعبه؛ مستندات کامل در Swagger مشترک برنامه. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/branch")
public class BranchController {
    private final BranchService service;
    private final BaseCatalogQueryService queries;
    @GetMapping public Page<BranchDto> findAll(@PageableDefault(size=20, sort="id") Pageable pageable) { return queries.branches(pageable); }
    @GetMapping("/{id}") public BranchDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @PostMapping public BranchDto save(@RequestBody BranchDto dto) { return service.save(dto); }
    @PutMapping("/{id}") public BranchDto update(@PathVariable("id") Long id,@RequestBody BranchDto dto) { return service.update(id,dto); }
}
