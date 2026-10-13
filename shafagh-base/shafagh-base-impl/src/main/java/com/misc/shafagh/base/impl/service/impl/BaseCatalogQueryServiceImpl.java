package com.misc.shafagh.base.impl.service.impl;
import com.misc.shafagh.base.impl.service.api.BaseCatalogQueryService;
import com.misc.shafagh.base.api.dto.*;
import com.misc.shafagh.base.impl.repository.*;
import com.misc.shafagh.base.impl.dto.mapper.city.CityMapper;
import com.misc.shafagh.base.impl.dto.mapper.fund.FundMapper;
import com.misc.shafagh.base.impl.dto.mapper.branch.BranchMapper;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/** خواندن صفحه‌بندی‌شده برای REST؛ قراردادهای عمومی BASE مستقل از Spring باقی می‌مانند. */
@Service
@RequiredArgsConstructor
@Transactional(transactionManager="baseTransactionManager", readOnly=true)
public class BaseCatalogQueryServiceImpl implements BaseCatalogQueryService {
    private final CityRepository cities;
    private final FundRepository funds;
    private final BranchRepository branches;
    private final CityMapper cityMapper;
    private final FundMapper fundMapper;
    private final BranchMapper branchMapper;
    public Page<CityDto> cities(Pageable pageable) {
        Page<com.misc.shafagh.base.impl.entity.CityEntity> page=cities.findCities(pageable);
        var parentIds=page.stream().map(city->city.getParentId()).filter(Objects::nonNull).distinct().toList();
        Map<Long,String> provinces=cities.findAllById(parentIds).stream()
            .filter(city->Objects.equals(0L,city.getParentId()))
            .collect(Collectors.toMap(city->city.getId(),city->city.getName()));
        return page.map(city->{var dto=cityMapper.toDto(city);dto.setProvince(provinces.get(city.getParentId()));return dto;});
    }
    public Page<FundDto> funds(Pageable pageable) { return funds.findAll(pageable).map(fundMapper::toDto); }
    public Page<BranchDto> branches(Pageable pageable) { return branches.findAll(pageable).map(branchMapper::toDto); }
}
