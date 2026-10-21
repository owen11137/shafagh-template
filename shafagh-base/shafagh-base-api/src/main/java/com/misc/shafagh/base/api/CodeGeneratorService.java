package com.misc.shafagh.base.api;

/**
 * Allocates fixed-width ASCII identifiers. Callers validate the fund/branch,
 * authorization and registration request idempotency before using this service.
 * Codes and their fund/branch prefixes must never be recycled or changed.
 */
public interface CodeGeneratorService {

    /** Six-digit fund code + global eight-digit sequence. Gaps are allowed. */
    String generateCustomerCode(String fundCode);

    /**
     * Six-digit fund code + five-digit ordinal, starting at 00001 per fund.
     * Requires the writable registration transaction: allocation rolls back
     * together with branch registration. Do not publish the code before commit.
     */
    String generateBranchCode(String fundCode);

    /**
     * Eleven-digit branch code + six-digit ordinal + Luhn check digit.
     * Allocation commits independently before returning; failed registration
     * consumes the ordinal. The branch and its counter must already be committed.
     * Prefer calling before the long account-registration transaction to avoid
     * holding an outer connection while obtaining the allocation connection.
     */
    String generateAccountNumber(String branchCode);

    /**
     * Initializes NEXT_NUMBER = 1 in the fund-creation transaction.
     * Requires a writable transaction. Repeated initialization fails; it never
     * resets or overwrites an existing counter.
     */
    void initializeBranchCounter(String fundCode);

    /**
     * Initializes NEXT_NUMBER = 1 in the branch-creation transaction.
     * Requires a writable transaction. The counter becomes available for account
     * allocation only after this transaction commits.
     */
    void initializeAccountCounter(String branchCode);
}
