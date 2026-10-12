package com.misc.shafagh.cif.impl.repository;

import com.misc.shafagh.cif.impl.dto.request.CustomerFilterDTO;
import com.misc.shafagh.cif.impl.entity.CustomerEntity;
import com.misc.shafagh.cif.impl.entity.enums.CustomerType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<CustomerEntity> byFilter(CustomerFilterDTO filter, Collection<CustomerType> allowedTypes) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("customerType").in(allowedTypes));

            if (filter != null) {
                if (filter.getCustomerType() != null) {
                    predicates.add(cb.equal(root.get("customerType"), filter.getCustomerType()));
                }
                if (hasText(filter.getIdentifier())) {
                    predicates.add(cb.like(cb.lower(root.get("identifier")), like(filter.getIdentifier())));
                }
                if (filter.getRegistrationDate() != null) {
                    predicates.add(cb.equal(root.get("registrationDate"), filter.getRegistrationDate()));
                }
                if (filter.getCityId() != null) {
                    predicates.add(cb.equal(root.get("city").get("id"), filter.getCityId()));
                }
                if (hasText(filter.getCustomerCode())) {
                    predicates.add(cb.like(cb.lower(root.get("customerCode")), like(filter.getCustomerCode())));
                }
                if (hasText(filter.getShahabCode())) {
                    predicates.add(cb.like(cb.lower(root.get("shahabCode")), like(filter.getShahabCode())));
                }
                if (hasText(filter.getFirstName())) {
                    predicates.add(cb.like(cb.lower(root.get("firstName")), like(filter.getFirstName())));
                }
                if (hasText(filter.getLastName())) {
                    predicates.add(cb.like(cb.lower(root.get("lastName")), like(filter.getLastName())));
                }
                if (hasText(filter.getFatherName())) {
                    predicates.add(cb.like(cb.lower(root.get("fatherName")), like(filter.getFatherName())));
                }
                if (filter.getDeathDate() != null) {
                    predicates.add(cb.equal(root.get("deathDate"), filter.getDeathDate()));
                }
                if (filter.getMaritalStatus() != null) {
                    predicates.add(cb.equal(root.get("maritalStatus"), filter.getMaritalStatus()));
                }
                if (filter.getGender() != null) {
                    predicates.add(cb.equal(root.get("gender"), filter.getGender()));
                }
                if (filter.getCustomerStatus() != null) {
                    predicates.add(cb.equal(root.get("customerStatus"), filter.getCustomerStatus()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String like(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}
