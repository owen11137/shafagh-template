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
- Declare supported constraint names directly in entity annotations (@UniqueConstraint,
  @ForeignKey, explicit @CheckConstraint) using the patterns above. Do not add
  naming strategies, metadata naming integrators, or SQL rename operations.
  Standard JPA cannot name PKs and some automatically generated constraints;
  retain standard Hibernate/Oracle names for those instead of custom naming code.
  Declare one-to-one unique constraints explicitly at table level.
  ShafaghOracleDialect only handles missing-object drops and sequence options;
  it must not assign constraint/index names or emit ALTER ... RENAME.
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
- Add meaningful Persian Jakarta Persistence comment attributes on @Table, @Column and @JoinColumn to every mapped table and physical column, including tenant columns and owning join columns. Inherited framework audit columns
use the JAR mappings directly without @AttributeOverrides. Verify COMMENT ON COLUMN statements in generated Oracle DDL.
- Do not add package-info.java files; the user requested their removal.

## Runtime transactions and schema administration

Runtime defaults to one datasource connecting as shafagh_app_user, one persistence
unit, and one transaction manager shared by all module aliases. Main ddl-auto is
validate because this user has DML privileges. Schema ownership and SchemaName
annotations stay in the domain modules. Do not create users or grant scripts;
the user provisioned the runtime account and its grants. Owner/legacy mode is
explicitly enabled with shafagh.persistence.module-datasources=true only for
schema administration or compatibility tests; its independent managers cannot
provide cross-module atomicity. Recreating Oracle tables removes object grants,
which must be re-established before returning to shared runtime mode. Workflows
use in-process calls with REQUIRED propagation and propagate failures; do not use
REQUIRES_NEW or independent managers in an atomic workflow. Local transactions
do not encompass separate REST services, external messages, or file operations.

## Service packages

Within each domain implementation module, keep internal service interfaces in
`service.api` and their implementations/helpers in `service.impl`. Do not use
`service.port` or place implementation classes directly in `service`. Concrete
service implementations use the `Impl` suffix. Public contracts in the separate
`shafagh-*-api` Maven modules retain their public API packages and cross-module
boundary rules. Internal `impl.service.api` interfaces may refer to the same
module's DTOs and pagination types; they are not public module API contracts.

## HTTP conventions

Use POST for new business lookup endpoints as requested by the user. Customer
identifier lookup accepts a String containing only ASCII digits, at most 20
characters, preserving leading zeros, and returns EXIST or NOT_EXIST. Resolve
fundId from Hibernate's current tenant resolver and include it explicitly in
identifier existence queries; never accept the lookup fund from the client.

## Swagger operation documentation

Place every REST method's summary and functional description in @Operation above
that controller method, and each controller's category/description in @Tag on the
class. Do not override these through central OpenAPI customizers or global tag
lists. Central schema/DTO and pagination documentation may remain. Do not add
@ApiResponse or @ApiResponses annotations; the user explicitly removed them.

## Framework BaseEntity

Use the real `com.misc.infra:framework-4:4.1.15` dependency and
`com.misc.infra.framework4.entity.BaseEntity`; do not recreate a local substitute.
For disconnected cloud builds install the uploaded real JAR with its embedded POM
into Maven's local repository. Do not commit the private binary. Common excludes
unneeded framework transitives and declares managed Envers for its annotations.
Use the framework BaseEntity audit-column mappings directly; the user requested
removal of @AttributeOverrides. Do not reintroduce overrides for these fields.
