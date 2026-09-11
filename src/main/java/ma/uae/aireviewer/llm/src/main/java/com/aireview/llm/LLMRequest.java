package com.aireview.llm;

/**
 * Immutable Data Transfer Object (DTO) representing a request to an LLM provider.
 *
 * <p>Using a Java {@code record} ensures immutability by construction — all fields are final and
 * set only at creation time. This prevents accidental mutation of a request after it has been
 * built and passed to a provider.
 *
 * <p><b>Field semantics:</b>
 * <ul>
 *   <li>{@code model}       — identifier of the model to use (e.g., {@code "gemma2:2b"}).
 *   <li>{@code prompt}      — the fully assembled prompt string. This field is treated as
 *                            <em>sensitive</em>: it must never be logged in plaintext because it
 *                            wraps untrusted source code between injection-defense markers.
 *   <li>{@code temperature} — sampling temperature in [0.0, 1.0]. Lower values make output more
 *                            deterministic, which is preferred for structured JSON responses.
 *   <li>{@code maxTokens}   — upper bound on generated tokens; acts as a circuit-breaker against
 *                            runaway model responses.
 * </ul>
 *
 * <p><b>Validation:</b> Basic pre-condition checks are applied in the compact constructor to
 * detect programming errors early (fail-fast principle).
 *
 * @param model       the model identifier; must not be null or blank
 * @param prompt      the assembled prompt; must not be null or blank
 * @param temperature sampling temperature; must be in range [0.0, 1.0]
 * @param maxTokens   maximum number of tokens to generate; must be positive
 */
public record LLMRequest(String model, String prompt, double temperature, int maxTokens) {

    // Compact constructor: validation runs before the canonical constructor assigns fields.
    public LLMRequest {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("LLMRequest: 'model' must not be null or blank.");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("LLMRequest: 'prompt' must not be null or blank.");
        }
        if (temperature < 0.0 || temperature > 1.0) {
            throw new IllegalArgumentException(
                    "LLMRequest: 'temperature' must be in [0.0, 1.0], got: " + temperature);
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException(
                    "LLMRequest: 'maxTokens' must be positive, got: " + maxTokens);
        }
    }

    /**
     * Convenience factory method for production use with sensible defaults.
     *
     * <p>Uses {@code temperature = 0.1} (near-deterministic) and {@code maxTokens = 2048}.
     *
     * @param model  the model identifier
     * @param prompt the assembled prompt
     * @return a new {@code LLMRequest} with default parameters
     */
    public static LLMRequest withDefaults(String model, String prompt) {
        return new LLMRequest(model, prompt, 0.1, 2048);
    }

    /**
     * Returns a safe, non-sensitive string representation.
     *
     * <p>The prompt is intentionally omitted to prevent accidental logging of source code
     * (which may be included verbatim in the prompt).
     */
    @Override
    public String toString() {
        return "LLMRequest[model=" + model + ", temperature=" + temperature
                + ", maxTokens=" + maxTokens + ", promptLength=" + prompt.length() + "]";
    }
}
