# Entity and database naming rules

Apply these user-provided rules to every new or changed entity in this repository.
For future supplied/new entities with naming violations, first identify the exact
violations and propose concrete names, then ask the user whether to correct them.
Do not correct such future violations until the user answers. Newly authored
entities should follow the rules from the outset. The current naming cleanup is
already authorized and does not require another confirmation.

- Keep entities/repositories in their domain implementation module. Cross-module
  links use IDs and public API services, not other modules' implementation classes.
- Use the relevant SchemaName constant on tables and sequences: BASE, CIF, DP, LN.
- PK: PK_<TABLE>.
- FK: FK_<CHILD_TABLE>_<PARENT_TABLE>. When multiple relationships share the same
  parent, add a meaningful role: FK_<CHILD>_<PARENT>_<ROLE>.
- UK: UK_<TABLE>_<COLUMN>; concatenate columns for a composite unique constraint.
- CHECK: CHK_<TABLE>_<RULE>. Existing enum/boolean checks use the column as the
  rule name; NOT NULL checks use <COLUMN>_NOT_NULL. Do not invent new business
  checks or change the permitted data solely to rename constraints.
- Name all constraints explicitly; avoid Oracle-generated SYS_* and hash names.
  Hibernate PK, implicit one-to-one UK and enum/boolean CHECK names are handled by
  DatabaseNamingConfiguration/ConstraintNamingIntegrator. Oracle NOT NULL names
  and backing PK/UK index names are handled by ShafaghOracleDialect.
- Index names: IDX_<TABLE>_<COL>, UDX_<TABLE>_<COL> for unique indexes,
  IDX_<TABLE>_<COL1>_<COL2> for composites, FDI_<TABLE>_<DESC> for function indexes,
  BDX_<TABLE>_<COL> for bitmap indexes. Use FDI from the rule table; do not silently
  switch to FBI from the user's illustrative example.
- Index and sequence names must be at most 50 characters. Do not shorten with
  random hashes; discuss an explicit, traceable shorter name if needed.
- Do not add performance indexes without consultation with the user/DBA/data
  architecture. Bitmap indexes are prohibited in OLTP. New indexes on existing
  production tables require ONLINE. Renaming an existing backing index does not
  create or rebuild it. No new optional indexes were authorized by this cleanup.
- Sequence: SEQ_<TABLE>_<COLUMN>; one sequence per key column. Explicit CACHE >=20,
  >=50 for RAC; NOCYCLE mandatory for PKs. This application uses CACHE 50 NOCYCLE
  NOORDER. AllocationSize=1 is separate from the database CACHE setting. ORDER
  in RAC needs a deliberate DBA decision due to performance costs.
- FK activation in production is a DBA decision; do not automatically disable
  FKs. If disabled deliberately, preserve integrity through application checks
  and integration tests.
- Materialized views are restricted to RPT or architecture approval. Document
  COMPLETE/FAST/FORCE and ON DEMAND/ON COMMIT refresh policy.
- Renaming existing Oracle objects requires reviewed migration scripts and a
  maintenance/deployment plan. ddl-auto=update is not a rename migration. Preserve
  sequence values; never recreate a populated table's sequence from 1.
- Verify actual constraint names in the database and generated Oracle DDL, not
  only Java annotations. Oracle DDL generation is not a live Oracle integration
  test; report the distinction.
- Do not add package-info.java files; the user requested their removal.
