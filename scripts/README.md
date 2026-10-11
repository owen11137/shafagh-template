# Oracle multi-schema deployment (DBA-managed)

The application deliberately **does not create or update database objects at startup**.
Default `spring.jpa.hibernate.ddl-auto=none`. Set `DB_DDL_AUTO=validate` only when
you want Hibernate to verify the installed schema during a deployment smoke test.

## Order for a new environment

1. DBA reviews and executes `01-create-schemas-and-user.sql` **only if needed**.
   If the schemas already exist, skip it. Each owner must be allowed to create its
   own tables and sequences, with tablespace quota. Never commit real passwords.
2. DBA executes `02-create-tables.sql` for the four basic integration-test entities.
3. DBA executes `05-create-cif-domain.sql` for the additional CIF customer,
   city, fund and branch entities (12 tables and 12 sequences).
4. DBA substitutes the actual connection user for **APP_USER** in
   `03-grants.sql` and `06-grants-cif-domain.sql`, then executes both grant files.
   If connecting directly as an owner, grants to that same owner are unnecessary.
5. Configure `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` **outside Git**.
   The application should use one connection user with appropriate object grants.
6. On staging, set `DB_DDL_AUTO=validate` and start the app. Only deploy to
   production after resolving any mapping mismatches and reviewing the SQL with
   the DBA against the actual Oracle release. Restore `DB_DDL_AUTO=none` if
   that is the site's production policy.

## Operational cautions

- Oracle DDL performs implicit commits. These scripts are intended for a new
  database or an explicitly approved maintenance window; they are **not idempotent**.
  Do not rerun `CREATE TABLE`/`CREATE SEQUENCE` against existing objects.
- The starter tables in `02-create-tables.sql` alone are **not sufficient** for
  the current CIF implementation.
- Sequences must be initialized beyond any existing IDs when deploying to an
  environment with imported data.
- Avoid granting `CREATE ANY TABLE` or `CREATE ANY SEQUENCE` to the application's
  runtime user. The DBA or schema owners deploy the SQL separately.
- `04-grants-test-only.sql` grants DELETE for tests and must **not** run in
  production. Additional CIF deletes, if required for a test DB, need separate
  reviewed grants.
- These scripts have undergone source review and CI compilation, **not actual
  execution against your Oracle server**. Oracle installation smoke testing,
  privileges and `validate` remain required before operational acceptance.
