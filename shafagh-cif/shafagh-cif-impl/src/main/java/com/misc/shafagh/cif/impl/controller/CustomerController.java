package com.misc.shafagh.cif.impl.controller;

import com.misc.shafagh.cif.impl.dto.request.CustomerFilterDTO;
import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.cif.impl.dto.response.CustomerDto;
import com.misc.shafagh.cif.impl.service.port.CustomerLegalService;
import com.misc.shafagh.cif.impl.service.port.CustomerRealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerRealService customerRealService;
    private final CustomerLegalService customerLegalService;

    @PostMapping("/real/save")
    public CustomerDto saveReal(@RequestBody CustomerReqDTO request) {
        return customerRealService.save(request);
    }

    @PostMapping("/real/update")
    public CustomerDto updateReal(@RequestBody CustomerReqDTO request) {
        return customerRealService.update(request);
    }

    @PostMapping("/real/getById/{id}")
    public CustomerDto getByIdReal(@PathVariable("id") Long id) {
        return customerRealService.getById(id);
    }

    @PostMapping("/real/filter")
    public Page<CustomerDto> searchReal(@RequestBody CustomerFilterDTO filter, Pageable pageable) {
        return customerRealService.search(filter, pageable);
    }

    @PostMapping("/legal/save")
    public CustomerDto saveLegal(@RequestBody CustomerReqDTO request) {
        return customerLegalService.save(request);
    }

    @PostMapping("/legal/update")
    public CustomerDto updateLegal(@RequestBody CustomerReqDTO request) {
        return customerLegalService.update(request);
    }

    @PostMapping("/legal/getById/{id}")
    public CustomerDto getByIdLegal(@PathVariable("id") Long id) {
        return customerLegalService.getById(id);
    }

    @PostMapping("/legal/filter")
    public Page<CustomerDto> searchLegal(@RequestBody CustomerFilterDTO filter, Pageable pageable) {
        return customerLegalService.search(filter, pageable);
    }

    @PostMapping("/{identifier}")
    public CustomerDto getByIdentifier(@PathVariable("identifier") String identifier) {
        return customerLegalService.getByIdentifier(identifier);
    }
}
