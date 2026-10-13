package com.misc.shafagh.dpst.impl.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
/** شماره ملی مشتری ثبت‌شده و کد محصول سپرده انتخاب‌شده. */
public record OpenDepositRequest(
    @NotBlank @Pattern(regexp="[0-9]{10}") String nationalCode,
    @NotBlank @Size(max=255) String productCode) {}
