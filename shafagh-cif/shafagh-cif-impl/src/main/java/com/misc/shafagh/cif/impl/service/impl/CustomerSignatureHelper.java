package com.misc.shafagh.cif.impl.service.impl;

import com.misc.shafagh.core.dto.request.SignatureReqDTO;
import com.misc.shafagh.core.entity.CustomerEntity;
import com.misc.shafagh.core.entity.SignatureEntity;
import com.misc.shafagh.core.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomerSignatureHelper {

    public void sync(CustomerEntity person, List<SignatureReqDTO> dtos) {
        if (dtos == null) {
            return;
        }

        Map<Long, SignatureEntity> existing = new HashMap<>();
        for (SignatureEntity s : person.getSignatures()) {
            if (s.getId() != null) {
                existing.put(s.getId(), s);
            }
        }

        Set<SignatureEntity> result = Collections.newSetFromMap(new IdentityHashMap<>());
        List<SignatureEntity> ordered = new ArrayList<>();

        for (SignatureReqDTO dto : dtos) {
            validateDates(dto);

            SignatureEntity signature;
            if (dto.getId() == null) {
                signature = new SignatureEntity();
                signature.setCustomer(person);
                signature.setFundId(dto.getFundId());
                signature.setFromDate(dto.getFromDate() != null ? dto.getFromDate() : new Date());
            } else {
                signature = existing.get(dto.getId());
                if (signature == null) {
                    throw new ValidationException("امضا با شناسه " + dto.getId() + " متعلق به این شخص نیست");
                }
                signature.setFromDate(dto.getFromDate());
            }

            signature.setToDate(dto.getToDate());
            signature.setDescription(dto.getDescription());
            applyImage(signature, dto);

            result.add(signature);
            ordered.add(signature);
        }

        person.getSignatures().removeIf(s -> !result.contains(s));

        Set<SignatureEntity> current = Collections.newSetFromMap(new IdentityHashMap<>());
        current.addAll(person.getSignatures());
        for (SignatureEntity s : ordered) {
            if (current.add(s)) {
                person.getSignatures().add(s);
            }
        }
    }

    private void applyImage(SignatureEntity signature, SignatureReqDTO dto) {
        String base64 = dto.getImage();
        if (base64 == null) {
            return;
        }
        if (base64.isEmpty()) {
            signature.setImage(null);
            return;
        }

        try {
            String pureBase64 = base64.contains(",") ? base64.substring(base64.indexOf(',') + 1) : base64;
            signature.setImage(Base64.getDecoder().decode(pureBase64));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("فرمت تصویر امضا نامعتبر است");
        }
    }

    private void validateDates(SignatureReqDTO dto) {
        if (dto.getFromDate() != null && dto.getToDate() != null && dto.getToDate().before(dto.getFromDate())) {
            throw new ValidationException("تاریخ پایان امضا نباید قبل از تاریخ شروع باشد");
        }
    }
}
