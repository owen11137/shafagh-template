package com.misc.shafagh.base.impl.service.impl;
import com.misc.shafagh.base.api.FundService;
import com.misc.shafagh.base.api.dto.FundDto;
import com.misc.shafagh.base.impl.dto.mapper.fund.FundMapper;
import com.misc.shafagh.base.impl.entity.fund.FundEntity;
import com.misc.shafagh.base.impl.repository.*;
import com.misc.shafagh.cif.api.CustomerLookup;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
@Transactional("baseTransactionManager")
public class FundServiceImpl implements FundService {
    private final FundRepository repository;
    private final FundMapper mapper;
    private final CustomerLookup customers;
    @Transactional(transactionManager = "baseTransactionManager", readOnly=true)
    public List<FundDto> findAll() { return repository.findAll().stream().map(mapper::toDto).toList(); }
    @Transactional(transactionManager = "baseTransactionManager", readOnly=true)
    public FundDto getById(Long id) { return mapper.toDto(require(id)); }
    public FundDto save(FundDto dto) {
        validate(dto);
        if(dto.getId()!=null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New fund must not have an ID");
        FundEntity entity=mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }
    public FundDto update(Long id, FundDto dto) {
        FundEntity entity=require(id);validate(dto);mapper.update(dto,entity);
        return mapper.toDto(repository.save(entity));
    }
    private FundEntity require(Long id) {
        if(id==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID is required");
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Fund not found"));
    }
    private void validate(FundDto dto) {
        if(dto==null || dto.getName()==null || dto.getName().isBlank() || dto.getCode()==null || dto.getCode().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Code and name are required");
        if(!customers.exists(dto.getOwnerId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Owner customer not found");
    }
}
