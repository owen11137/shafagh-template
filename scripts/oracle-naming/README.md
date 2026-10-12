# Oracle naming migration

Run each SQL file as its matching owner in the same PDB used by the application:
`shfq_base.sql`, `shfq_cif.sql`, `shfq_dpst.sql`, `shfq_loan.sql`.

1. Review the SQL and back up the database. Arrange deployment with the DBA.
2. Stop all application writers, including older versions of the application.
3. Run the matching owner script **before** starting the version that uses SEQ_*.
4. Verify names and sequence values, then start the new application version.

The scripts rename the old sequences without resetting their values, explicitly
set CACHE 50 NOCYCLE NOORDER, rename existing PK/FK/UK and single-column generated
CHECK/NOT NULL constraints, and rename existing PK/UK backing indexes to UDX_*.
They neither create performance indexes nor rebuild existing indexes. The ONLINE
rule applies when creating new indexes on existing production tables, not rename.

These files assume Oracle 19c+ (including USER_CONSTRAINTS.SEARCH_CONDITION_VC).
They do not connect to the database themselves and have not been run against the
user's Oracle instance. Oracle DDL commits implicitly, so migration is not atomic.
Already renamed objects are skipped on rerun. If both old and new sequences exist,
the script stops: the DBA must reconcile their current values and existing IDs;
never restart either sequence from 1. Review current sequence-to-column mappings
before running, especially if the live database differs from the repository.

Existing explicitly named CHK_* checks are preserved; unsupported/multi-column
unnamed checks require a DBA-selected rule name. Multiple FKs to one parent use
the child column names without _ID as role suffixes. Uniqueness rules and enum
ORDINAL representations remain unchanged.

Verification as each owner:

```sql
SELECT table_name, constraint_name, constraint_type, status
FROM user_constraints
ORDER BY table_name, constraint_type, constraint_name;

SELECT sequence_name, increment_by, cache_size, cycle_flag, order_flag, last_number
FROM user_sequences
ORDER BY sequence_name;

SELECT table_name, index_name, uniqueness, status
FROM user_indexes
ORDER BY table_name, index_name;
```

Generated fresh-schema Oracle DDL is available after `mvn clean install` in
`shafagh-app/target/oracle-naming-ddl.sql`. It is an offline verification artifact,
not a script to execute on a populated production database. Fresh schema creation
is handled by Hibernate with ShafaghOracleDialect; rename migrations are separate.
