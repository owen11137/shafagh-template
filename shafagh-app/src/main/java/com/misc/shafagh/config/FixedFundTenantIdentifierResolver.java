package com.misc.shafagh.config;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
/** Temporary fixed fund resolver. Replace this implementation with authenticated JWT tenant resolution later. */
public final class FixedFundTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {
    private final long fundId;
    public FixedFundTenantIdentifierResolver(long fundId) {
        if (fundId <= 0) throw new IllegalArgumentException("shafagh.fund-id must be positive");
        this.fundId = fundId;
    }
    @Override public Long resolveCurrentTenantIdentifier() { return fundId; }
    @Override public boolean validateExistingCurrentSessions() { return true; }
}
