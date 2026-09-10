package com.aireview.llm;

/**
 * Immutable Data Transfer Object (DTO) representing a raw response from an LLM provider.
 *
 * <p>This record intentionally holds only the raw string content returned by the model.
 * Higher-level concerns — such as JSON parsing, schema validation, and retry decisions — are
 * deliberately kept out of this class and handled by {@code ResilientEvaluator} (Task 5).
 * This separation ensures the DTO layer remains simple, testable, and provider-agnostic.
 *
 * <p><b>Field semantics:</b>
 * <ul>
 *   <li>{@code rawContent}     — the exact text produced by the LLM; never null (may be empty).
 *   <li>{@code modelUsed}      — the model identifier that actually generated the response
 *                               (may differ from the requested model if the provider auto-selects).
 *   <li>{@code durationMillis} — wall-clock time in milliseconds for the provider round-trip;
 *                               useful for performance metrics and timeout tuning.
 * </ul>
 *
 * @param rawContent     the raw text output from the model; never null
 * @param modelUsed      the model that generated the response; never null or blank
 * @param durationMillis non-negative duration of the provider call in milliseconds
 */
public record LLMResponse(String rawContent, String modelUsed, long durationMillis) {

    // Compact constructor: fail-fast validation.
    public LLMResponse {
        if (rawContent == null) {
            throw new IllegalArgumentException("LLMResponse: 'rawContent' must not be null.");
        }
        if (modelUsed == null || modelUsed.isBlank()) {
            throw new IllegalArgumentException(
                    "LLMResponse: 'modelUsed' must not be null or blank.");
        }
        if (durationMillis < 0) {
            throw new IllegalArgumentException(
                    "LLMResponse: 'durationMillis' must be non-negative, got: " + durationMillis);
        }
    }

    /**
     * Returns {@code true} if the raw content appears to contain a JSON object or array.
     *
     * <p>This is a <em>heuristic</em> pre-check only — it does not validate JSON syntax.
     * It is used by the resilience layer to fast-fail on clearly non-JSON responses before
     * attempting a full parse.
     *
     * @return true if trimmed content starts with '{' or '['
     */
    public boolean looksLikeJson() {
        String trimmed = rawContent.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    /**
     * Returns a safe, concise representation for logging purposes.
     * Only the first 80 characters of content are shown to avoid flooding logs.
     */
    @Override
    public String toString() {
        String preview = rawContent.length() > 80
                ? rawContent.substring(0, 80) + "..."
                : rawContent;
        return "LLMResponse[model=" + modelUsed + ", durationMs=" + durationMillis
                + ", content=\"" + preview + "\"]";
    }
}
