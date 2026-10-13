package com.misc.shafagh.common.context;
/** Resolve at operation time; implementations may later read the authenticated SecurityContext. */
@FunctionalInterface
public interface CurrentUserContext {
    UserContext current();
    default long fundId() { return current().fundId(); }
    default long userId() { return current().userId(); }
    default long branchId() { return current().branchId(); }
}
