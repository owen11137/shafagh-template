package com.misc.shafagh.cif.impl.service;

import com.misc.shafagh.base.api.BusinessException;
import com.misc.shafagh.cif.api.PersonService;
import com.misc.shafagh.cif.api.dto.CreatePersonRequest;
import com.misc.shafagh.cif.api.dto.PersonDto;
import com.misc.shafagh.cif.api.dto.PersonStatus;
import com.misc.shafagh.cif.impl.entity.PersonEntity;
import com.misc.shafagh.cif.impl.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class PersonServiceImpl implements PersonService {

    private final PersonRepository repository;

    PersonServiceImpl(PersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PersonDto create(CreatePersonRequest request) {
        if (repository.existsByNationalCode(request.nationalCode())) {
            throw new BusinessException("PERSON_DUPLICATE", "National code already exists");
        }
        PersonEntity saved = repository.save(
                new PersonEntity(request.nationalCode(), request.firstName(), request.lastName()));
        return toDto(saved);
    }

    @Override
    public PersonDto getById(Long id) {
        return repository.findById(id)
                .map(PersonServiceImpl::toDto)
                .orElseThrow(() -> new BusinessException("PERSON_NOT_FOUND", "Person not found: " + id));
    }

    @Override
    public boolean existsActive(Long id) {
        return repository.findById(id).map(p -> p.getStatus() == PersonStatus.ACTIVE).orElse(false);
    }

    private static PersonDto toDto(PersonEntity e) {
        return new PersonDto(e.getId(), e.getNationalCode(), e.getFirstName(), e.getLastName(), e.getStatus());
    }
}
