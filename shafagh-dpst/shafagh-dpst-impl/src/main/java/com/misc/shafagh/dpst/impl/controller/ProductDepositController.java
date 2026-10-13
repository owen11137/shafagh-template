package com.misc.shafagh.dpst.impl.controller;

import com.misc.shafagh.dpst.impl.dto.request.ProductDepositReqDTO;
import com.misc.shafagh.dpst.impl.dto.response.ProductDepositResponseDto;
import com.misc.shafagh.dpst.impl.service.port.ProductDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** کنترلر DPST برای ثبت، دریافت، فهرست، ویرایش و حذف محصول سپرده صندوق فعال. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dpst/products")
public class ProductDepositController {
    private final ProductDepositService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDepositResponseDto create(@Valid @RequestBody ProductDepositReqDTO request) { return service.create(request); }

    @GetMapping("/{id}")
    public ProductDepositResponseDto getById(@PathVariable("id") Long id) { return service.getById(id); }

    @GetMapping
    public Page<ProductDepositResponseDto> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) { return service.findAll(pageable); }

    @PutMapping("/{id}")
    public ProductDepositResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductDepositReqDTO request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) { service.delete(id); }
}
