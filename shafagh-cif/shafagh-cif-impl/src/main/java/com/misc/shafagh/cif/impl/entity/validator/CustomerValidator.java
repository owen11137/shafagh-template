package com.misc.shafagh.cif.impl.entity.validator;

import com.misc.shafagh.cif.impl.dto.request.CustomerReqDTO;
import com.misc.shafagh.cif.impl.exception.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerValidator {


    public void validateCommon(CustomerReqDTO dto) {
        List<String> errors = new ArrayList<>();

        if (dto == null) {
            throw new ValidationException("درخواست نمی‌تواند خالی باشد");
        }

        if (!StringUtils.hasText(dto.getIdentifier())) {
            errors.add("کد شناسایی (identifier) الزامی است");
        }

        if (dto.getCustomerType() == null) {
            errors.add("نوع شخص (personType) الزامی است");
        }

        if (dto.getRegistrationDate() == null) {
            errors.add("تاریخ ثبت (registrationDate) الزامی است");
        }

        if (dto.getCityId() == null) {
            errors.add("شناسه شهر (cityId) الزامی است");
        }

        if (dto.getCustomerStatus() == null) {
            errors.add("وضعیت شخص (personStatus) الزامی است");
        }

        // حداقل یک آدرس
        if (CollectionUtils.isEmpty(dto.getAddresses())) {
            errors.add("حداقل یک آدرس الزامی است");
        } else {
            validateAddresses(dto, errors);
        }

        if (CollectionUtils.isEmpty(dto.getContacts())) {
            errors.add("حداقل یک راه ارتباطی الزامی است");
        }


        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }


    public void validateReal(CustomerReqDTO dto) {
        List<String> errors = new ArrayList<>();

        if (!StringUtils.hasText(dto.getFirstName())) {
            errors.add("نام (firstName) برای شخص حقیقی الزامی است");
        }
        if (!StringUtils.hasText(dto.getLastName())) {
            errors.add("نام خانوادگی (lastName) برای شخص حقیقی الزامی است");
        }
        if (!StringUtils.hasText(dto.getFatherName())) {
            errors.add("نام پدر (fatherName) برای شخص حقیقی الزامی است");
        }
        if (dto.getGender() == null) {
            errors.add("جنسیت (gender) برای شخص حقیقی الزامی است");
        }
        if (dto.getMaritalStatus() == null) {
            errors.add("وضعیت تأهل (maritalStatus) برای شخص حقیقی الزامی است");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }


    public void validateLegal(CustomerReqDTO dto) {


    }

    private void validateAddresses(CustomerReqDTO dto, List<String> errors) {
        for (int i = 0; i < dto.getAddresses().size(); i++) {
            var addr = dto.getAddresses().get(i);
            if (addr.getCityId() == null) {
                errors.add("شناسه شهر برای آدرس شماره " + (i + 1) + " الزامی است");
            }
            if (addr.getAddressType() == null) {
                errors.add("نوع آدرس برای آدرس شماره " + (i + 1) + " الزامی است");
            }
        }
    }
}