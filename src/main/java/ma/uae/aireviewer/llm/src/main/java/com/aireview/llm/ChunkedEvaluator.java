package com.aireview.llm;

import java.util.ArrayList;
import java.util.List;

/**
 * High-level evaluator that handles projects whose source code exceeds the LLM context window.
 *
 * <p>When a project's source code is small enough to fit in one chunk, {@code ChunkedEvaluator}
 * behaves identically to {@link ResilientEvaluator}. When the source is too large, it:
 * <ol>
 *   <li>Splits the source into chunks via {@link ContextSplitter}.</li>
 *   <li>Evaluates each chunk independently using {@link ResilientEvaluator} for a configurable
 *       subset of criteria.</li>
 *   <li>Aggregates the per-chunk {@link CriterionResult} objects into a single result per
 *       criterion using {@link ContextSplitter#aggregateChunkResults}.</li>
 *   <li>Assembles the aggregated per-criterion results into a final {@link EvaluationResult}.</li>
 * </ol>
 *
 * <p><b>PATTERN: Strategy</b> — {@link LLMProvider} is injected; {@link ResilientEvaluator}
 * is used internally and inherits the retry/validation resilience from Task 5.
 */
public class ChunkedEvaluator {

    private final ResilientEvaluator resilientEvaluator;
    private final ContextSplitter    splitter;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Creates a {@code ChunkedEvaluator} with custom components.
     *
     * @param resilientEvaluator the retry-wrapped evaluator; must not be null
     * @param splitter           the context splitter; must not be null
     */
    public ChunkedEvaluator(ResilientEvaluator resilientEvaluator, ContextSplitter splitter) {
        if (resilientEvaluator == null) {
            throw new IllegalArgumentException("ChunkedEvaluator: resilientEvaluator must not be null.");
        }
        if (splitter == null) {
            throw new IllegalArgumentException("ChunkedEvaluator: splitter must not be null.");
        }
        this.resilientEvaluator = resilientEvaluator;
        this.splitter           = splitter;
    }

    /**
     * Creates a {@code ChunkedEvaluator} with a default {@link ContextSplitter}.
     *
     * @param provider the LLM provider; must not be null
     */
    public ChunkedEvaluator(LLMProvider provider) {
        this(new ResilientEvaluator(provider), new ContextSplitter());
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Evaluates a project, automatically splitting large source files into chunks.
     *
     * <p>If the source fits in one chunk, this is equivalent to a single
     * {@link ResilientEvaluator#evaluate} call. Otherwise, each chunk is evaluated
     * independently and results are aggregated.
     *
     * @param projectName the name of the evaluated project
     * @param sourceCode  the raw concatenated source code (may exceed context window)
     * @param model       the model identifier
     * @return a fully populated {@link EvaluationResult}
     * @throws LLMException if any criterion evaluation fails after all retries
     */
    public EvaluationResult evaluate(String projectName, String sourceCode, String model)
            throws LLMException {
        return evaluate(projectName, sourceCode, model,
                List.of(EvaluationCriterion.values()));
    }

    /**
     * Evaluates a project against a specific list of criteria, with automatic chunking.
     *
     * @param projectName the project name
     * @param sourceCode  the raw source code
     * @param model       the model identifier
     * @param criteria    the criteria to evaluate
     * @return a fully populated {@link EvaluationResult}
     * @throws LLMException if any criterion evaluation fails after all retries
     */
    public EvaluationResult evaluate(String projectName, String sourceCode, String model,
                                     List<EvaluationCriterion> criteria) throws LLMException {

        if (projectName == null || projectName.isBlank()) {
            throw new IllegalArgumentException("projectName must not be null or blank.");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model must not be null or blank.");
        }
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException("criteria must not be null or empty.");
        }

        // Step 1 — Split the source code.
        List<String> chunks = splitter.split(sourceCode);

        // Step 2 — Fast path: single chunk (most common case).
        if (chunks.size() == 1) {
            return resilientEvaluator.evaluate(projectName, chunks.get(0), model, criteria);
        }

        // Step 3 — Multi-chunk path: evaluate each chunk per criterion.
        // Collect per-criterion lists of chunk results.
        long   totalStart = System.currentTimeMillis();
        int    totalScore = 0;
        int    maxTotal   = 0;
        List<CriterionResult> aggregatedResults = new ArrayList<>();

        for (EvaluationCriterion criterion : criteria) {
            List<CriterionResult> chunkResults = new ArrayList<>();

            for (int chunkIdx = 0; chunkIdx < chunks.size(); chunkIdx++) {
                String chunkLabel = projectName + " [chunk " + (chunkIdx + 1)
                        + "/" + chunks.size() + "]";

                // Evaluate a single criterion for this chunk (single-criterion evaluation).
                EvaluationResult chunkEval = resilientEvaluator.evaluate(
                        chunkLabel, chunks.get(chunkIdx), model, List.of(criterion));

                chunkResults.add(chunkEval.results().get(0));
            }

            // Step 4 — Aggregate chunk results for this criterion.
            CriterionResult aggregated = ContextSplitter.aggregateChunkResults(chunkResults);
            aggregatedResults.add(aggregated);
            totalScore += aggregated.score();
            maxTotal   += aggregated.maxScore();
        }

        long duration = System.currentTimeMillis() - totalStart;
        return new EvaluationResult(projectName, aggregatedResults, totalScore, maxTotal, duration);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** @return the underlying {@link ContextSplitter}. */
    public ContextSplitter   getSplitter()           { return splitter; }

    /** @return the underlying {@link ResilientEvaluator}. */
    public ResilientEvaluator getResilientEvaluator() { return resilientEvaluator; }
}
