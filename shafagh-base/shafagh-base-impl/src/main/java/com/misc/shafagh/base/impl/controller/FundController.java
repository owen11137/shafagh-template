package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.FundService;
import com.misc.shafagh.base.api.dto.FundDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/fund")
public class FundController {
    private final FundService service;
    @GetMapping public List<FundDto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public FundDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @PostMapping public FundDto save(@RequestBody FundDto dto) { return service.save(dto); }
    @PutMapping("/{id}") public FundDto update(@PathVariable("id") Long id,@RequestBody FundDto dto) { return service.update(id,dto); }
}
