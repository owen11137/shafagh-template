package com.misc.shafagh.orchestration.service.api;

/** An in-process database workflow whose steps participate in a single shared transaction. */
public interface AtomicProcessExecutor {
    <T> T execute(Work<T> work) throws Exception;

    @FunctionalInterface
    interface Work<T> {
        T run() throws Exception;
    }
}
