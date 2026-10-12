package com.misc.shafagh.config.persistence;

import java.util.Locale;
import java.util.stream.Collectors;
import org.hibernate.boot.model.naming.*;

/** Human-readable fallback names, including Hibernate's implicit one-to-one unique keys. */
public class DatabaseNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {
    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        return Identifier.toIdentifier("UK_" + source.getTableName().getText().toUpperCase(Locale.ROOT)
                + "_" + source.getColumnNames().stream().map(id -> id.getText().toUpperCase(Locale.ROOT)).collect(Collectors.joining("_")));
    }
    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        return Identifier.toIdentifier("FK_" + source.getTableName().getText().toUpperCase(Locale.ROOT)
                + "_" + source.getReferencedTableName().getText().toUpperCase(Locale.ROOT));
    }
    @Override
    public Identifier determineIndexName(ImplicitIndexNameSource source) {
        String name = "IDX_" + source.getTableName().getText().toUpperCase(Locale.ROOT) + "_"
                + source.getColumnNames().stream().map(id -> id.getText().toUpperCase(Locale.ROOT)).collect(Collectors.joining("_"));
        if (name.length() > 50) throw new IllegalArgumentException("Index name exceeds 50 characters: " + name);
        return Identifier.toIdentifier(name);
    }
}
