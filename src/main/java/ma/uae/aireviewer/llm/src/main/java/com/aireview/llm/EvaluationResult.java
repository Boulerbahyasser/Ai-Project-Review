package com.aireview.llm;

import java.util.List;
import java.util.Objects;

/**
 * Aggregated evaluation result for an entire project review session.
 *
 * <p>An {@code EvaluationResult} is produced by {@link ResilientEvaluator} after it has
 * successfully called the LLM for each {@link EvaluationCriterion}, parsed and validated
 * every {@link CriterionResult}, and assembled the final report.
 *
 * <p><b>Immutability:</b> Like all DTOs in this subsystem, this is a {@code record}, ensuring
 * all fields are final and set once at construction.
 *
 * @param projectName    the name of the evaluated project
 * @param results        the per-criterion results in evaluation order; never null, never empty
 * @param totalScore     the sum of all per-criterion scores
 * @param maxTotalScore  the sum of all per-criterion max scores
 * @param durationMillis total wall-clock time for all LLM calls combined
 */
public record EvaluationResult(
        String             projectName,
        List<CriterionResult> results,
        int                totalScore,
        int                maxTotalScore,
        long               durationMillis
) {

    public EvaluationResult {
        Objects.requireNonNull(projectName, "EvaluationResult: projectName must not be null.");
        Objects.requireNonNull(results,     "EvaluationResult: results must not be null.");
        if (results.isEmpty()) {
            throw new IllegalArgumentException("EvaluationResult: results must not be empty.");
        }
        // Defensive copy — caller cannot mutate our list.
        results        = List.copyOf(results);
        durationMillis = Math.max(0, durationMillis);
    }

    /**
     * Returns the overall score as a percentage of the total possible score.
     *
     * @return percentage in [0.0, 100.0]; 0.0 if maxTotalScore is 0
     */
    public double overallPercent() {
        return maxTotalScore > 0 ? (totalScore * 100.0) / maxTotalScore : 0.0;
    }

    /**
     * Returns a brief human-readable summary line suitable for logging.
     */
    public String summary() {
        return String.format("EvaluationResult[project='%s', score=%d/%d (%.1f%%), criteria=%d, durationMs=%d]",
                projectName, totalScore, maxTotalScore, overallPercent(),
                results.size(), durationMillis);
    }
}
