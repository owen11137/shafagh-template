package com.misc.shafagh.base.api;
import com.misc.shafagh.base.api.dto.FundDto;
import java.util.List;
public interface FundService {
    List<FundDto> findAll();
    FundDto getById(Long id);
    FundDto save(FundDto dto);
    FundDto update(Long id, FundDto dto);
}
