package com.misc.shafagh.loan.impl.controller;

import com.misc.shafagh.loan.impl.dto.request.ProductLoanReqDTO;
import com.misc.shafagh.loan.impl.dto.response.ProductLoanResponseDto;
import com.misc.shafagh.loan.impl.service.port.ProductLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** کنترلر LOAN برای ثبت، دریافت، فهرست، ویرایش و حذف محصول تسهیلات صندوق فعال. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/loan/products")
public class ProductLoanController {
    private final ProductLoanService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductLoanResponseDto create(@Valid @RequestBody ProductLoanReqDTO request) { return service.create(request); }

    @GetMapping("/{id}")
    public ProductLoanResponseDto getById(@PathVariable("id") Long id) { return service.getById(id); }

    @GetMapping
    public Page<ProductLoanResponseDto> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) { return service.findAll(pageable); }

    @PutMapping("/{id}")
    public ProductLoanResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductLoanReqDTO request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) { service.delete(id); }
}
