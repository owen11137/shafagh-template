package com.misc.shafagh.cif.api;

import com.misc.shafagh.cif.api.dto.CreatePersonRequest;
import com.misc.shafagh.cif.api.dto.PersonDto;

/** سرویس expose شده‌ی ماژول اشخاص (CIF). */
public interface PersonService {

    PersonDto create(CreatePersonRequest request);

    PersonDto getById(Long id);

    boolean existsActive(Long id);
}
