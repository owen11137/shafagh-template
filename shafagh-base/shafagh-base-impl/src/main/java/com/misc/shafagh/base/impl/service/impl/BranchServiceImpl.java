package com.misc.shafagh.base.impl.service.impl;
import com.misc.shafagh.base.api.BranchService;
import com.misc.shafagh.base.api.dto.BranchDto;
import com.misc.shafagh.base.impl.dto.mapper.branch.BranchMapper;
import com.misc.shafagh.base.impl.entity.branch.BranchEntity;
import com.misc.shafagh.base.impl.repository.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
@Transactional("baseTransactionManager")
public class BranchServiceImpl implements BranchService {
    private final BranchRepository repository;
    private final BranchMapper mapper;
    private final FundRepository funds;
    @org.springframework.beans.factory.annotation.Value("${shafagh.fund-id:1}") private long fundId;
    @Transactional(transactionManager = "baseTransactionManager", readOnly=true)
    public List<BranchDto> findAll() { return repository.findAll().stream().map(mapper::toDto).toList(); }
    @Transactional(transactionManager = "baseTransactionManager", readOnly=true)
    public BranchDto getById(Long id) { return mapper.toDto(require(id)); }
    public BranchDto save(BranchDto dto) {
        validate(dto);
        if(dto.getId()!=null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New branch must not have an ID");
        BranchEntity entity=mapper.toEntity(dto);entity.setFund(funds.getReferenceById(dto.getFundId()));
        return mapper.toDto(repository.save(entity));
    }
    public BranchDto update(Long id, BranchDto dto) {
        BranchEntity entity=require(id);validate(dto);mapper.update(dto,entity);entity.setFund(funds.getReferenceById(dto.getFundId()));
        return mapper.toDto(repository.save(entity));
    }
    private BranchEntity require(Long id) {
        if(id==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID is required");
        return repository.findByIdAndFundId(id, fundId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Branch not found"));
    }
    private void validate(BranchDto dto) {
        if(dto==null || dto.getName()==null || dto.getName().isBlank() || dto.getCode()==null || dto.getCode().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Code and name are required");
        if(dto.getFundId()==null) dto.setFundId(fundId);
        if(dto.getFundId()!=fundId) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Branch must belong to current fund");
        if(!funds.existsById(dto.getFundId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Fund not found");
    }
}
