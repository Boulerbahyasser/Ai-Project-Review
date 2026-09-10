package com.aireview.llm;

/**
 * PATTERN: Strategy + Adapter
 *
 * <p>This interface is the central abstraction of the LLM subsystem. It acts as:
 *
 * <ul>
 *   <li><b>Strategy:</b> The calling code (e.g., {@code ResilientEvaluator}) programs against this
 *       interface. Any concrete implementation ({@code OllamaProvider}, {@code MockLLMProvider})
 *       can be swapped at runtime without modifying the evaluation logic.
 *   <li><b>Adapter:</b> Each concrete implementation adapts a specific backend API (local HTTP,
 *       cloud REST, mock) to this single, unified contract. The rest of the system never sees the
 *       raw HTTP or JSON details of any particular provider.
 * </ul>
 *
 * <p><b>Security note:</b> Implementations MUST NOT log or expose the raw {@code prompt} field, as
 * it may contain sensitive source-code snippets wrapped in untrusted-input markers.
 */
public interface LLMProvider {

    /**
     * Sends an {@link LLMRequest} to the underlying language model and returns a raw
     * {@link LLMResponse}.
     *
     * <p>Contract guarantees:
     * <ul>
     *   <li>This method is <em>synchronous</em>; it blocks until the model responds or a timeout
     *       is reached.
     *   <li>On any unrecoverable error (network failure, HTTP 5xx after retries), the method MUST
     *       throw an {@link LLMException} rather than returning a partial or null response.
     *   <li>The returned {@link LLMResponse#rawContent()} is guaranteed to be non-null; it may
     *       be an empty string if the model returns no tokens, but never {@code null}.
     * </ul>
     *
     * @param request the fully constructed request (model name, prompt, parameters); never null
     * @return a non-null {@link LLMResponse} wrapping the model's raw text output
     * @throws LLMException if the underlying call fails and cannot be recovered internally
     */
    LLMResponse call(LLMRequest request) throws LLMException;
}
