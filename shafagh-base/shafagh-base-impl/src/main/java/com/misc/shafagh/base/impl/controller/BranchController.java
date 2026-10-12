package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.BranchService;
import com.misc.shafagh.base.api.dto.BranchDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/** کنترلر REST ماژول BASE برای مدیریت شعبه؛ مستندات کامل در Swagger مشترک برنامه. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/branch")
public class BranchController {
    private final BranchService service;
    @GetMapping public List<BranchDto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public BranchDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @PostMapping public BranchDto save(@RequestBody BranchDto dto) { return service.save(dto); }
    @PutMapping("/{id}") public BranchDto update(@PathVariable("id") Long id,@RequestBody BranchDto dto) { return service.update(id,dto); }
}
