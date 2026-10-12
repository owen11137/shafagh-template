package com.misc.shafagh.base.api;
import com.misc.shafagh.base.api.dto.BranchDto;
import java.util.List;
public interface BranchService {
    List<BranchDto> findAll();
    BranchDto getById(Long id);
    BranchDto save(BranchDto dto);
    BranchDto update(Long id, BranchDto dto);
}
