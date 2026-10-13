package com.misc.shafagh.config.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.internal.StandardSequenceExporter;
import org.hibernate.tool.schema.internal.StandardTableExporter;
import org.hibernate.tool.schema.spi.Exporter;

/** Oracle-specific DDL: explicitly cached sequences and named NOT NULL/backing indexes. */
public class ShafaghOracleDialect extends OracleDialect {
    public ShafaghOracleDialect() { super(); }
    public ShafaghOracleDialect(DialectResolutionInfo info) { super(info); }

    @Override
    public Exporter<Sequence> getSequenceExporter() {
        return new StandardSequenceExporter(this) {
            @Override
            public String[] getSqlCreateStrings(Sequence sequence, Metadata metadata, SqlStringGenerationContext context) {
                if (sequence.getName().getSequenceName().getText().length() > 50) {
                    throw new IllegalArgumentException("Sequence name exceeds 50 characters");
                }
                String[] sql = super.getSqlCreateStrings(sequence, metadata, context);
                for (int i = 0; i < sql.length; i++) {
                    if (sql[i].toLowerCase(Locale.ROOT).startsWith("create sequence")) {
                        sql[i] += " NOCYCLE CACHE 50 NOORDER";
                    }
                }
                return sql;
            }
            @Override
            public String[] getSqlDropStrings(Sequence sequence, Metadata metadata, SqlStringGenerationContext context) {
                return ignoreMissing(super.getSqlDropStrings(sequence, metadata, context), -2289);
            }
        };
    }

    @Override
    public Exporter<Table> getTableExporter() {
        return new StandardTableExporter(this) {
            @Override
            public String[] getSqlCreateStrings(Table table, Metadata metadata, SqlStringGenerationContext context) {
                List<String> statements = new ArrayList<>(Arrays.asList(super.getSqlCreateStrings(table, metadata, context)));
                for (int i = 0; i < statements.size(); i++) {
                    String sql = statements.get(i);
                    if (!sql.toLowerCase(Locale.ROOT).startsWith("create table")) continue;
                    for (var column : table.getColumns()) {
                        if (!column.isNullable()) {
                            String rule = "CHK_" + table.getName().toUpperCase(Locale.ROOT) + "_" + column.getName().toUpperCase(Locale.ROOT) + "_NOT_NULL";
                            String pattern = "(?i)((?<![A-Z0-9_])" + Pattern.quote(column.getQuotedName(dialect)) + "\\s+"
                                    + Pattern.quote(column.getSqlType(metadata)) + ".*?)\\bnot null\\b";
                            sql = sql.replaceFirst(pattern, "$1constraint " + rule + " not null");
                        }
                    }
                    statements.set(i, sql);
                    if (table.getPrimaryKey() != null) {
                        statements.add(renameIndex(table, table.getPrimaryKey().getName(), table.getPrimaryKey().getColumns()));
                    }
                    for (var unique : table.getUniqueKeys().values()) {
                        if (sql.contains(unique.getName())) statements.add(renameIndex(table, unique.getName(), unique.getColumns()));
                    }
                }
                return statements.toArray(String[]::new);
            }
            @Override
            public String[] getSqlDropStrings(Table table, Metadata metadata, SqlStringGenerationContext context) {
                return ignoreMissing(super.getSqlDropStrings(table, metadata, context), -942);
            }
        };
    }

    @Override
    public Exporter<UniqueKey> getUniqueKeyExporter() {
        var delegate = super.getUniqueKeyExporter();
        return new Exporter<>() {
            public String[] getSqlCreateStrings(UniqueKey key, Metadata metadata, SqlStringGenerationContext context) {
                String[] sql = delegate.getSqlCreateStrings(key, metadata, context);
                if (Arrays.stream(sql).noneMatch(command -> command.toLowerCase(Locale.ROOT).contains("add constraint"))) return sql;
                List<String> statements = new ArrayList<>(Arrays.asList(sql));
                statements.add(renameIndex(key.getTable(), key.getName(), key.getColumns()));
                return statements.toArray(String[]::new);
            }
            public String[] getSqlDropStrings(UniqueKey key, Metadata metadata, SqlStringGenerationContext context) {
                return ignoreMissing(delegate.getSqlDropStrings(key, metadata, context), -942, -2443);
            }
        };
    }

    @Override
    public Exporter<ForeignKey> getForeignKeyExporter() {
        var delegate = super.getForeignKeyExporter();
        return new Exporter<>() {
            public String[] getSqlCreateStrings(ForeignKey key, Metadata metadata, SqlStringGenerationContext context) {
                return delegate.getSqlCreateStrings(key, metadata, context);
            }
            public String[] getSqlDropStrings(ForeignKey key, Metadata metadata, SqlStringGenerationContext context) {
                return ignoreMissing(delegate.getSqlDropStrings(key, metadata, context), -942, -2443);
            }
        };
    }

    /** Oracle versions without DROP IF EXISTS: suppress only the expected missing-object codes. */
    private String[] ignoreMissing(String[] commands, int... codes) {
        String allowed = Arrays.stream(codes).mapToObj(Integer::toString).collect(Collectors.joining(", "));
        return Arrays.stream(commands).map(sql -> "begin execute immediate '" + sql.replace("'", "''")
                + "'; exception when others then if SQLCODE not in (" + allowed
                + ") then raise; end if; end;").toArray(String[]::new);
    }

    private String renameIndex(Table table, String oldName, List<org.hibernate.mapping.Column> columns) {
        String name = "UDX_" + table.getName().toUpperCase(Locale.ROOT) + "_"
                + columns.stream().map(c -> c.getName().toUpperCase(Locale.ROOT)).collect(Collectors.joining("_"));
        if (name.length() > 50) throw new IllegalArgumentException("Index name exceeds 50 characters: " + name);
        return "alter index " + (table.getSchema() == null ? "" : table.getSchema() + ".") + oldName + " rename to " + name;
    }
}
