package com.misc.shafagh.cif.api.dto;

public record PersonDto(Long id, String nationalCode, String firstName, String lastName, PersonStatus status) {
}
