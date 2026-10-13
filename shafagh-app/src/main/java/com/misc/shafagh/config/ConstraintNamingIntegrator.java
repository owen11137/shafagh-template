package com.misc.shafagh.config;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;

/** Assigns PK and generated enum/boolean check names before Hibernate exports schema DDL. */
public class ConstraintNamingIntegrator implements Integrator {
    @Override
    public void integrate(Metadata metadata, BootstrapContext context, SessionFactoryImplementor factory) {
        for (var namespace : metadata.getDatabase().getNamespaces()) {
            for (var table : namespace.getTables()) {
                String name = table.getName().toUpperCase(Locale.ROOT);
                if (table.getPrimaryKey() != null) {
                    var primary = table.getPrimaryKey();
                    primary.setName("PK_" + name);
                    // StandardTableExporter reads its explicit name from the ordering key.
                    var ordering = new org.hibernate.mapping.UniqueKey(table);
                    ordering.setName("PK_" + name); ordering.setNameExplicit(true);
                    primary.getColumns().forEach(ordering::addColumn);
                    primary.setOrderingUniqueKey(ordering);
                }
                for (var column : table.getColumns()) {
                    if (column.isUnique()) {
                        String keyName = "UK_" + name + "_" + column.getName().toUpperCase(Locale.ROOT);
                        var key = table.getOrCreateUniqueKey(keyName);
                        key.addColumn(column);
                        // Keep uniqueness at table level so its name is exported explicitly.
                        column.setUnique(false);
                    }
                }
                table.getUniqueKeys().values().forEach(key -> {
                    key.setName("UK_" + name + "_"
                        + key.getColumns().stream().map(c -> c.getName().toUpperCase(Locale.ROOT)).collect(Collectors.joining("_")));
                    key.setNameExplicit(true); key.setExplicit(true);
                });
                Map<String, Long> parentCounts = table.getForeignKeys().values().stream()
                        .collect(Collectors.groupingBy(fk -> fk.getReferencedTable().getName(), Collectors.counting()));
                table.getForeignKeys().values().forEach(fk -> {
                    String parent = fk.getReferencedTable().getName().toUpperCase(Locale.ROOT);
                    String role = parentCounts.get(fk.getReferencedTable().getName()) > 1
                            ? "_" + fk.getColumns().stream().map(c -> c.getName().toUpperCase(Locale.ROOT).replaceFirst("_ID$", "")).collect(Collectors.joining("_")) : "";
                    fk.setName("FK_" + name + "_" + parent + role);
                });
                for (var column : table.getColumns()) {
                    int number = 0;
                    for (var check : column.getCheckConstraints()) {
                        if (check.isAnonymous()) check.setName("CHK_" + name + "_" + column.getName().toUpperCase(Locale.ROOT) + (number++ == 0 ? "" : "_" + number));
                    }
                }
                for (var check : table.getChecks()) {
                    if (check.isAnonymous()) throw new IllegalArgumentException("Table check needs an explicit rule name: " + name);
                }
            }
        }
    }
}
