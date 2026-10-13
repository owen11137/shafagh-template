package com.misc.shafagh.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

public class ShafaghOracleDialectResolver implements DialectResolver {
    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        return "Oracle".equalsIgnoreCase(info.getDatabaseName()) ? new ShafaghOracleDialect(info) : null;
    }
}
