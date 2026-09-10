package com.aireview.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Resilience layer that orchestrates the full evaluation pipeline with JSON schema validation
 * and an exponential back-off retry loop.
 *
 * <p><b>Responsibilities:</b>
 * <ol>
 *   <li>For each {@link EvaluationCriterion}, build a prompt via {@link PromptBuilder},
 *       wrap it in an {@link LLMRequest}, and call the injected {@link LLMProvider}.</li>
 *   <li>Validate the raw response: heuristic JSON pre-check ({@code looksLikeJson()}),
 *       Jackson parse, and {@link CriterionResult#isValid()} semantic check.</li>
 *   <li>On any failure (provider exception, malformed JSON, invalid schema), retry up to
 *       {@link #maxRetries} times using exponential back-off with a configurable base delay.</li>
 *   <li>Aggregate all per-criterion results into an {@link EvaluationResult}.</li>
 * </ol>
 *
 * <p><b>PATTERN: Strategy</b> — The {@link LLMProvider} dependency is injected via the
 * constructor and used only through the interface. Swapping {@code OllamaProvider} for
 * {@code MockLLMProvider} requires no code change in this class.
 *
 * <p><b>Resilience contract:</b>
 * <ul>
 *   <li>If a criterion succeeds on any attempt within the retry budget, its result is kept.</li>
 *   <li>If a criterion exhausts all retries, an {@link LLMException} is thrown immediately,
 *       aborting the entire evaluation. Partial results are not returned.</li>
 *   <li>The back-off delay is computed as {@code baseDelayMs * 2^attempt} (capped at
 *       {@link #MAX_BACKOFF_MS}) to avoid overwhelming a recovering provider.</li>
 * </ul>
 *
 * <p><b>Security note:</b> Raw LLM responses are never logged. Only sanitised metadata
 * (attempt count, criterion name, error type) is surfaced in exception messages.
 */
public class ResilientEvaluator {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /** Default maximum number of retry attempts per criterion (initial attempt + 2 retries). */
    public static final int  DEFAULT_MAX_RETRIES    = 3;

    /** Default base delay in milliseconds for the first retry. */
    public static final long DEFAULT_BASE_DELAY_MS  = 500L;

    /** Hard ceiling on back-off delay regardless of attempt number. */
    static final long MAX_BACKOFF_MS = 16_000L;

    // -----------------------------------------------------------------------
    // Fields (PATTERN: Strategy — provider typed as interface)
    // -----------------------------------------------------------------------

    private final LLMProvider  provider;
    private final ObjectMapper mapper;
    private final int          maxRetries;
    private final long         baseDelayMs;
    private final boolean      sleepEnabled; // set to false in tests

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Full constructor for test use — allows disabling Thread.sleep to keep tests fast.
     *
     * @param provider     the LLM provider; must not be null
     * @param maxRetries   maximum total attempts per criterion; must be positive
     * @param baseDelayMs  base back-off delay in ms; must be non-negative
     * @param sleepEnabled if {@code false}, Thread.sleep is skipped (test mode)
     */
    ResilientEvaluator(LLMProvider provider, int maxRetries, long baseDelayMs,
                       boolean sleepEnabled) {
        if (provider    == null) throw new IllegalArgumentException("provider must not be null.");
        if (maxRetries  <= 0)   throw new IllegalArgumentException("maxRetries must be positive.");
        if (baseDelayMs < 0)    throw new IllegalArgumentException("baseDelayMs must be non-negative.");
        this.provider     = provider;
        this.mapper       = new ObjectMapper();
        this.maxRetries   = maxRetries;
        this.baseDelayMs  = baseDelayMs;
        this.sleepEnabled = sleepEnabled;
    }

    /**
     * Production constructor using default retry parameters with sleep enabled.
     *
     * @param provider the LLM provider; must not be null
     */
    public ResilientEvaluator(LLMProvider provider) {
        this(provider, DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY_MS, true);
    }

    /**
     * Production constructor with custom retry parameters.
     *
     * @param provider    the LLM provider; must not be null
     * @param maxRetries  maximum total attempts per criterion; must be positive
     * @param baseDelayMs base back-off delay in ms; must be non-negative
     */
    public ResilientEvaluator(LLMProvider provider, int maxRetries, long baseDelayMs) {
        this(provider, maxRetries, baseDelayMs, true);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Evaluates a project against all defined {@link EvaluationCriterion} values.
     *
     * <p>Criteria are evaluated sequentially. Each criterion gets up to {@link #maxRetries}
     * attempts. Failures are retried with exponential back-off. The first irrecoverable
     * criterion failure aborts the evaluation and throws {@link LLMException}.
     *
     * @param projectName the name of the project being evaluated
     * @param sourceCode  the raw source code to evaluate; treated as untrusted
     * @param model       the model identifier (e.g., {@code "gemma2:2b"})
     * @return a fully populated {@link EvaluationResult}
     * @throws LLMException if any criterion exhausts its retry budget
     */
    public EvaluationResult evaluate(String projectName, String sourceCode, String model)
            throws LLMException {

        if (projectName == null || projectName.isBlank()) {
            throw new IllegalArgumentException("projectName must not be null or blank.");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model must not be null or blank.");
        }

        List<CriterionResult> results     = new ArrayList<>();
        long                  totalStart  = System.currentTimeMillis();
        int                   totalScore  = 0;
        int                   maxTotal    = 0;

        for (EvaluationCriterion criterion : EvaluationCriterion.values()) {
            CriterionResult result = evaluateWithRetry(criterion, projectName, sourceCode, model);
            results.add(result);
            totalScore += result.score();
            maxTotal   += result.maxScore();
        }

        long duration = System.currentTimeMillis() - totalStart;
        return new EvaluationResult(projectName, results, totalScore, maxTotal, duration);
    }

    /**
     * Evaluates a single criterion against a specific list of criteria (for partial evaluation).
     *
     * @param projectName the project name
     * @param sourceCode  the raw source code
     * @param model       the model identifier
     * @param criteria    the specific criteria to evaluate
     * @return a fully populated {@link EvaluationResult}
     * @throws LLMException if any criterion exhausts its retry budget
     */
    public EvaluationResult evaluate(String projectName, String sourceCode, String model,
                                     List<EvaluationCriterion> criteria) throws LLMException {
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException("criteria list must not be null or empty.");
        }

        List<CriterionResult> results    = new ArrayList<>();
        long                  totalStart = System.currentTimeMillis();
        int                   totalScore = 0;
        int                   maxTotal   = 0;

        for (EvaluationCriterion criterion : criteria) {
            CriterionResult result = evaluateWithRetry(criterion, projectName, sourceCode, model);
            results.add(result);
            totalScore += result.score();
            maxTotal   += result.maxScore();
        }

        long duration = System.currentTimeMillis() - totalStart;
        return new EvaluationResult(projectName, results, totalScore, maxTotal, duration);
    }

    // -----------------------------------------------------------------------
    // Retry loop
    // -----------------------------------------------------------------------

    /**
     * Evaluates a single criterion with exponential back-off retry.
     *
     * <p>Each attempt:
     * <ol>
     *   <li>Builds the prompt via {@link PromptBuilder}.</li>
     *   <li>Calls the provider.</li>
     *   <li>Performs a heuristic JSON pre-check ({@link LLMResponse#looksLikeJson()}).</li>
     *   <li>Parses the raw content into a {@link CriterionResult} via Jackson.</li>
     *   <li>Calls {@link CriterionResult#isValid()} to verify semantic constraints.</li>
     * </ol>
     *
     * <p>On any failure, the exception is captured, the back-off delay is applied,
     * and the next attempt begins. After {@code maxRetries} failed attempts the last
     * exception is re-thrown wrapped in an {@link LLMException}.
     *
     * @throws LLMException if all attempts fail
     */
    private CriterionResult evaluateWithRetry(EvaluationCriterion criterion,
                                              String projectName,
                                              String sourceCode,
                                              String model) throws LLMException {
        Exception lastException = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {

            // Apply back-off before retrying (not before the first attempt).
            if (attempt > 0) {
                applyBackoff(attempt);
            }

            try {
                // Step 1 — Build the prompt (injection-safe).
                String prompt = PromptBuilder
                        .forCriterion(criterion)
                        .withSourceCode(sourceCode)
                        .withProjectName(projectName)
                        .build();

                // Step 2 — Call the provider.
                LLMRequest  request  = LLMRequest.withDefaults(model, prompt);
                LLMResponse response = provider.call(request);

                // Step 3 — Extract JSON from the raw response.
                // Small models often wrap the JSON in prose or markdown fences like
                // ```json { ... } ```. extractJson() strips the wrapper and returns
                // the first outermost { ... } block it finds, or throws LLMException
                // if no extractable JSON object is present at all.
                String jsonContent = extractJson(response.rawContent(), criterion, attempt + 1);

                // Step 4 — Parse JSON into CriterionResult.
                CriterionResult result;
                try {
                    result = mapper.readValue(jsonContent, CriterionResult.class);
                } catch (JsonProcessingException e) {
                    throw new LLMException(
                            "Criterion '" + criterion.getDisplayName() + "' attempt " + (attempt + 1)
                            + ": JSON parse failure — " + e.getOriginalMessage(), e);
                }

                // Step 5 — Semantic validation.
                if (!result.isValid()) {
                    throw new LLMException(
                            "Criterion '" + criterion.getDisplayName() + "' attempt " + (attempt + 1)
                            + ": parsed result failed isValid() check "
                            + "(score=" + result.score() + ", maxScore=" + result.maxScore() + ").");
                }

                // Success — return immediately.
                return result;

            } catch (LLMException e) {
                lastException = e;
                // Loop continues to next attempt.
            }
        }

        // All attempts exhausted.
        throw new LLMException(
                "Criterion '" + criterion.getDisplayName() + "' failed after "
                + maxRetries + " attempt(s). Last error: "
                + (lastException != null ? lastException.getMessage() : "unknown"),
                lastException);
    }

    // -----------------------------------------------------------------------
    // Back-off helper
    // -----------------------------------------------------------------------

    /**
     * Applies exponential back-off: {@code baseDelayMs * 2^(attempt-1)}, capped at
     * {@link #MAX_BACKOFF_MS}. In test mode ({@code sleepEnabled=false}), this is a no-op.
     *
     * @param attempt 1-based retry attempt index (1 = first retry)
     */
    private void applyBackoff(int attempt) {
        if (!sleepEnabled) return;

        long delay = Math.min(baseDelayMs * (1L << (attempt - 1)), MAX_BACKOFF_MS);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt flag
        }
    }

    // -----------------------------------------------------------------------
    // Accessors (for testing / observability)
    // -----------------------------------------------------------------------

    /** @return the maximum number of attempts per criterion. */
    public int  getMaxRetries()   { return maxRetries; }

    /** @return the base back-off delay in milliseconds. */
    public long getBaseDelayMs()  { return baseDelayMs; }

    // -----------------------------------------------------------------------
    // JSON extraction helper
    // -----------------------------------------------------------------------

    /**
     * Extracts the first outermost JSON object ({@code \{...\}}) from a raw LLM response.
     *
     * <p>Small models (e.g., gemma2:2b) frequently wrap their JSON output in prose or
     * markdown code fences despite being instructed not to, e.g.:
     * <pre>
     *   Here is the evaluation:
     *   ```json
     *   { "criterion": "Architecture", ... }
     *   ```
     * </pre>
     * This method handles all such variants by:
     * <ol>
     *   <li>Stripping leading/trailing whitespace.</li>
     *   <li>Removing markdown code fences (\`\`\`json ... \`\`\` or \`\`\` ... \`\`\`).</li>
     *   <li>Finding the index of the first {@code \{} and the matching closing {@code \}}
     *       using a bracket-depth counter.</li>
     *   <li>Returning the substring between them (inclusive).</li>
     * </ol>
     *
     * @param raw       the raw model output
     * @param criterion the criterion being evaluated (for error messages)
     * @param attempt   the current attempt number (for error messages)
     * @return the extracted JSON object string
     * @throws LLMException if no balanced JSON object can be found
     */
    static String extractJson(String raw, EvaluationCriterion criterion, int attempt)
            throws LLMException {
        if (raw == null || raw.isBlank()) {
            throw new LLMException(
                    "Criterion '" + criterion.getDisplayName() + "' attempt " + attempt
                    + ": empty response from model.");
        }

        // Step 1 — Strip markdown code fences if present anywhere in the response.
        String cleaned = raw.strip();
        int fenceStart = cleaned.indexOf("```");
        if (fenceStart != -1) {
            int fenceContentStart = cleaned.indexOf('\n', fenceStart);
            int fenceEnd = cleaned.lastIndexOf("```");
            if (fenceContentStart != -1 && fenceEnd > fenceContentStart) {
                cleaned = cleaned.substring(fenceContentStart + 1, fenceEnd).strip();
            }
        }

        // Step 2 — Find the JSON object. Prefer the one containing "criterion" if present.
        int criterionIdx = cleaned.indexOf("\"criterion\"");
        int start = -1;
        if (criterionIdx != -1) {
            start = cleaned.lastIndexOf('{', criterionIdx);
        }
        if (start == -1) {
            start = cleaned.indexOf('{');
        }

        if (start == -1) {
            throw new LLMException(
                    "Criterion '" + criterion.getDisplayName() + "' attempt " + attempt
                    + ": no JSON object found in model response.");
        }

        int depth = 0;
        boolean inString  = false;
        boolean escaped   = false;

        for (int i = start; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (c == '\\' && inString) { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return cleaned.substring(start, i + 1);
                    }
                }
            }
        }

        throw new LLMException(
                "Criterion '" + criterion.getDisplayName() + "' attempt " + attempt
                + ": JSON object in model response is not balanced (unclosed '{').");
    }
}
