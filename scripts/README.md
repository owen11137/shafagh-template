# Oracle DDL — DBA-managed, CIF production entities only

Only the current **CIF** domain entities are included: CITY, CUSTOMER,
CUSTOMER_ADDRESS, CUSTOMER_CONTACT, SIGNATURE, CUSTOMER_INQUIRY,
FUND, FUND_ADDRESS, FUND_CONTACT, BRANCH, BRANCH_ADDRESS and BRANCH_CONTACT.
The four prototype/test-only persistence entities (Currency, Person, Deposit,
Loan) have been removed along with their demonstration services and tests.
**Deposit and Loan modules remain as Maven module scaffolding** for the real implementations.

## Fresh installation (DBA)
1. Review `01-create-schemas-and-user.sql` and run only if schema owners do not exist.
   Replace all dummy passwords; provision privileges and quotas according to local policy.
2. Run **`02-create-tables.sql`** as DBA or CIF schema owner. This contains the 12
   CIF tables and their 12 sequences, plus same-schema foreign keys.
3. Replace `APP_USER` with the actual Oracle connection user in **`03-grants.sql`**,
   then execute it. The application account must not have schema-creation permissions.
4. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` outside source control.
5. Set `DB_DDL_AUTO=validate` on staging and run the app against Oracle to
   detect mapping mismatches. The default is `none`; Hibernate never creates tables.

## Safety
These SQL files are for a **new/empty CIF schema** and are NOT idempotent.
Never run against an existing schema without a reviewed migration plan and backup.
Oracle DDL implicitly commits, and GitHub Actions builds do not execute SQL against
your Oracle environment. A DBA must validate Oracle version, constraints, data
types, physical naming and access rights before any production deployment.
Old demo scripts 04/05/06 were retired and are not part of the install sequence.
