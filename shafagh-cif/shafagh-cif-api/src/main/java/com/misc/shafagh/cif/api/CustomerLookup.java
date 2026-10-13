package com.misc.shafagh.cif.api;
import java.util.Optional;
import com.misc.shafagh.cif.api.dto.CustomerExistenceStatus;
public interface CustomerLookup {
    boolean exists(Long customerId);
    /** Exact national-code/company-registration IDENTIFIER existence, explicitly scoped to the resolved current fund, regardless of customer type/status.
     * Null or blank input returns NOT_EXIST. */
    CustomerExistenceStatus existsByIdentifier(String identifier);
    /** Active Iranian individual in the current fund; implementation remains inside CIF. */
    Optional<Long> findActiveIndividualId(String nationalCode);
}
