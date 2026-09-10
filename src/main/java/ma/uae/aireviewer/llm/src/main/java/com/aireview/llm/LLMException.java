package com.aireview.llm;

/**
 * Checked exception representing a non-recoverable failure within the LLM subsystem.
 *
 * <p>Using a checked exception enforces that all callers of {@link LLMProvider#call} explicitly
 * handle or propagate failures, preventing silent error swallowing. The exception wraps the root
 * cause to preserve the full stack trace for diagnostics.
 */
public class LLMException extends Exception {

    /**
     * Constructs an {@code LLMException} with a descriptive message.
     *
     * @param message human-readable description of the failure
     */
    public LLMException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code LLMException} wrapping a root cause.
     *
     * @param message human-readable description of the failure
     * @param cause   the underlying exception (e.g., {@code IOException}, {@code JsonParseException})
     */
    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
}
