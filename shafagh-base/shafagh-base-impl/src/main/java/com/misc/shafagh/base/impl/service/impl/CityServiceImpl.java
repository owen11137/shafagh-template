package com.misc.shafagh.base.impl.service.impl;
import com.misc.shafagh.base.api.CityService;
import com.misc.shafagh.base.api.dto.CityDto;
import com.misc.shafagh.base.impl.dto.mapper.CityMapper;
import com.misc.shafagh.base.impl.entity.CityEntity;
import com.misc.shafagh.base.impl.repository.CityRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service
@RequiredArgsConstructor
@Transactional
public class CityServiceImpl implements CityService {
    private final CityRepository repository;
    private final CityMapper mapper;
    public List<CityDto> findAll() {
        return findAllIncludingProvinces().stream().filter(c -> !Objects.equals(0L,c.getParentId())).toList();
    }
    @Transactional(readOnly=true)
    public List<CityDto> findAllIncludingProvinces() {
        List<CityEntity> cities=repository.findAll();
        Map<Long,String> provinces=cities.stream().filter(c -> Objects.equals(0L,c.getParentId()))
            .collect(Collectors.toMap(CityEntity::getId,CityEntity::getName));
        return cities.stream().map(c -> { CityDto dto=mapper.toDto(c);dto.setProvince(provinces.get(c.getParentId()));return dto; }).toList();
    }
    @Transactional(readOnly=true)
    public CityDto getById(Long id) {
        CityEntity city=require(id);
        CityDto dto=mapper.toDto(city);
        if(city.getParentId()!=null && city.getParentId()!=0L) {
            repository.findById(city.getParentId()).ifPresent(p -> dto.setProvince(p.getName()));
        }
        return dto;
    }
    public CityDto save(CityDto dto) {
        validate(dto);
        if(dto.getId()!=null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"New city must not have an ID");
        CityEntity entity=mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }
    public CityDto update(Long id, CityDto dto) {
        CityEntity entity=require(id);validate(dto);
        Long entityId=entity.getId();mapper.updateEntityFromDto(dto,entity);entity.setId(entityId);
        return mapper.toDto(repository.save(entity));
    }
    private CityEntity require(Long id) {
        if(id==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"City ID is required");
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"City not found"));
    }
    private void validate(CityDto dto) {
        if(dto==null || dto.getName()==null || dto.getName().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"City name is required");
        if(dto.getParentId()!=null && dto.getParentId()!=0L) require(dto.getParentId());
    }
}
