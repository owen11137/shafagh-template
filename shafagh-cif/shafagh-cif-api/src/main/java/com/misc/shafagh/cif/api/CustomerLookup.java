package com.misc.shafagh.cif.api;
import java.util.Optional;
public interface CustomerLookup {
    boolean exists(Long customerId);
    /** Active Iranian individual in the current fund; implementation remains inside CIF. */
    Optional<Long> findActiveIndividualId(String nationalCode);
}
