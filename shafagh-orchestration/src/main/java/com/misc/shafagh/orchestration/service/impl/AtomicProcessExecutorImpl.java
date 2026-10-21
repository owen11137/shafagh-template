package com.misc.shafagh.orchestration.service.impl;

import com.misc.shafagh.orchestration.service.api.AtomicProcessExecutor;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

/** Registered only in shared persistence mode; checked and unchecked failures roll back the complete workflow. */
public class AtomicProcessExecutorImpl implements AtomicProcessExecutor {
    @Override
    @Transactional(transactionManager = "appTransactionManager", rollbackFor = Exception.class)
    public <T> T execute(Work<T> work) throws Exception {
        return Objects.requireNonNull(work, "Workflow is required").run();
    }
}
