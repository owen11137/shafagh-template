# shafagh-common

Shared technical infrastructure for the domain implementation modules. This module
must not depend on domain modules or application configuration.

`com.misc.shafagh.common.persistence` contains `BaseEntity` and `TenantAwareEntity`.
They are JPA mapped superclasses: their fields are stored in concrete entity tables,
and they have no tables of their own.

`com.misc.shafagh.common.product.ProductStatus` is the shared product status enum.
Its values are persisted as ordinals, so their existing order must be preserved.

Future general-purpose utilities belong in responsibility-specific packages such as
`common.time` or `common.validation`. Business logic, including installment
calculations, belongs in its owning domain and is exposed through that domain's API.

This artifact includes persistence dependencies. Domain API modules remain
framework-free and should not depend on it. Domain implementations may use common
infrastructure but must not access another domain's implementation. These boundaries
are checked by `ModuleBoundaryTest` in `shafagh-app`.
