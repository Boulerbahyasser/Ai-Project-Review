package com.aireview.llm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * JSON mapping record for a single criterion's evaluation result.
 *
 * <p>The LLM is instructed to return a JSON object matching this exact schema for every criterion
 * it evaluates. Using explicit {@code @JsonProperty} annotations decouples the Java field names
 * from the JSON keys, allowing the schema contract to evolve independently of the Java model.
 *
 * <p><b>Expected JSON structure:</b>
 * <pre>{@code
 * {
 *   "criterion": "SOLID Principles",
 *   "score":     7,
 *   "maxScore":  10,
 *   "feedback":  "Good use of SRP; DIP is violated in ServiceLocator.",
 *   "issues":    ["ServiceLocator couples high-level modules to implementations."]
 * }
 * }</pre>
 *
 * <p><b>Design note ({@code @JsonIgnoreProperties}):</b> The {@code ignoreUnknown = true} flag
 * prevents deserialization failures when the model returns additional fields (e.g., a "reasoning"
 * key sometimes emitted by chain-of-thought models). This makes the parser resilient to minor
 * schema drift without requiring a strict allowlist of every possible key.
 *
 * <p><b>Validation semantics:</b>
 * <ul>
 *   <li>{@code score} must be in [0, {@code maxScore}].
 *   <li>{@code maxScore} must be positive.
 *   <li>{@code criterion} and {@code feedback} must not be null or blank.
 * </ul>
 *
 * @param criterion human-readable name of the evaluated criterion (e.g., "Architecture")
 * @param score     points awarded; must be in [0, maxScore]
 * @param maxScore  maximum possible points for this criterion; must be positive
 * @param feedback  narrative explanation of the score
 * @param issues    list of specific issues found; may be null (treated as empty)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CriterionResult(
        @JsonProperty("criterion") String criterion,
        @JsonProperty("score")     int    score,
        @JsonProperty("maxScore")  int    maxScore,
        @JsonProperty("feedback")  String feedback,
        @JsonProperty("issues")    List<String> issues
) {

    /**
     * Jackson-compatible compact constructor.
     *
     * <p>The {@code @JsonCreator} is not strictly required for records in Jackson 2.12+, but is
     * included explicitly for clarity and forward-compatibility with older Jackson versions that
     * may be present in the broader project classpath.
     *
     * <p>A null {@code issues} list is normalised to an empty immutable list here so callers never
     * need to perform null checks.
     */
    @JsonCreator
    public CriterionResult(
            @JsonProperty("criterion") String criterion,
            @JsonProperty("score")     int    score,
            @JsonProperty("maxScore")  int    maxScore,
            @JsonProperty("feedback")  String feedback,
            @JsonProperty("issues")    List<String> issues
    ) {
        this.criterion = Objects.requireNonNull(criterion,
                "CriterionResult: 'criterion' must not be null.");
        this.maxScore  = maxScore;
        this.score     = score;
        this.feedback  = Objects.requireNonNull(feedback,
                "CriterionResult: 'feedback' must not be null.");
        // Normalise null issues list to empty list for safe iteration by callers.
        this.issues = (issues != null) ? List.copyOf(issues) : List.of();
    }

    /**
     * Validates the internal consistency of this result after deserialization.
     *
     * <p>Keeping validation separate from the constructor allows the JSON parser to construct
     * the object even with boundary-violating values, then let the resilience layer decide
     * whether to retry, clamp, or reject the response.
     *
     * @return {@code true} if all invariants hold
     */
    public boolean isValid() {
        if (criterion.isBlank()) return false;
        if (feedback.isBlank())  return false;
        if (maxScore <= 0)       return false;
        if (score < 0)           return false;
        if (score > maxScore)    return false;
        return true;
    }

    /**
     * Convenience accessor: score as a percentage of the maximum.
     *
     * @return percentage in [0.0, 100.0], or 0.0 if maxScore is 0 (guard against division by zero)
     */
    public double scorePercent() {
        return maxScore > 0 ? (score * 100.0) / maxScore : 0.0;
    }
}
