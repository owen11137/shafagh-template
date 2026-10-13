package com.misc.shafagh.base.impl.service.api;
import com.misc.shafagh.base.api.dto.CityDto;
import com.misc.shafagh.base.api.dto.FundDto;
import com.misc.shafagh.base.api.dto.BranchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
/** قرارداد داخلی خواندن صفحه‌بندی‌شده اطلاعات پایه. */
public interface BaseCatalogQueryService {
    Page<CityDto> cities(Pageable pageable);
    Page<FundDto> funds(Pageable pageable);
    Page<BranchDto> branches(Pageable pageable);
}
