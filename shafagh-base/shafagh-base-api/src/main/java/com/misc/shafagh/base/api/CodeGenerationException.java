package com.misc.shafagh.base.api;

/** Stable allocation failures; technical database failures retain their cause. */
public class CodeGenerationException extends RuntimeException {

    public enum Reason {
        COUNTER_NOT_INITIALIZED,
        COUNTER_ALREADY_INITIALIZED,
        CAPACITY_EXHAUSTED,
        INVALID_NUMBER_STATE
    }

    private final Reason reason;

    public CodeGenerationException(Reason reason, String message) {
        super(message);
        this.reason = java.util.Objects.requireNonNull(reason);
    }

    public CodeGenerationException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = java.util.Objects.requireNonNull(reason);
    }

    public Reason getReason() {
        return reason;
    }
}
