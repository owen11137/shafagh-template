package com.misc.shafagh.base.impl.controller;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.base.api.dto.CityDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/city")
public class CityController {
    private final CityService service;
    @GetMapping public List<CityDto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public CityDto getById(@PathVariable("id") Long id) { return service.getById(id); }
    @PostMapping public CityDto save(@RequestBody CityDto dto) { return service.save(dto); }
    @PutMapping("/{id}") public CityDto update(@PathVariable("id") Long id,@RequestBody CityDto dto) { return service.update(id,dto); }
}
