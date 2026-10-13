package com.misc.shafagh.common.context;
/** Immutable snapshot of the current fund, user and branch. No JWT or Spring dependency. */
public record UserContext(long fundId, long userId, long branchId) {
    public UserContext {
        if (fundId <= 0 || userId <= 0 || branchId <= 0)
            throw new IllegalArgumentException("fundId, userId and branchId must be positive");
    }
}
