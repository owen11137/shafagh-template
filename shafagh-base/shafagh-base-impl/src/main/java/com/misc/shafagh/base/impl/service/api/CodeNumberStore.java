package com.misc.shafagh.base.impl.service.api;

/** Database operations; counter writes require the caller's writable transaction. */
public interface CodeNumberStore {
    long MAX_BRANCH_NUMBER = 99_999L;
    long MAX_ACCOUNT_NUMBER = 999_999L;
    long MAX_CUSTOMER_NUMBER = 99_999_999L;

    long nextCustomerNumber();

    long allocateBranchNumber(String fundCode);

    long allocateAccountNumber(String branchCode);

    void initializeBranchCounter(String fundCode);

    void initializeAccountCounter(String branchCode);
}
