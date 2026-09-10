package com.aireview.llm;

/**
 * Typed enumeration of the evaluation criteria supported by the LLM subsystem.
 *
 * <p>Using an enum instead of raw strings provides:
 * <ul>
 *   <li>Compile-time safety — invalid criterion names are caught by the compiler.</li>
 *   <li>A single source of truth for criterion metadata (display name, max score,
 *       evaluation guidance) that the {@link PromptBuilder} uses to construct targeted prompts.</li>
 *   <li>Easy extensibility — adding a new criterion requires only a new enum constant.</li>
 * </ul>
 */
public enum EvaluationCriterion {

    /**
     * Assesses the overall layering, package structure, and module boundaries of the project.
     */
    ARCHITECTURE(
            "Architecture",
            10,
            "Evaluate the overall layered architecture, package structure, separation of concerns, "
            + "and module boundaries. Check for inappropriate coupling between layers."
    ),

    /**
     * Assesses adherence to the five SOLID object-oriented design principles.
     */
    SOLID_PRINCIPLES(
            "SOLID Principles",
            10,
            "Evaluate adherence to the SOLID principles: Single Responsibility, Open/Closed, "
            + "Liskov Substitution, Interface Segregation, and Dependency Inversion. "
            + "Cite specific classes or methods that violate each principle."
    ),

    /**
     * Assesses the presence, quality, and coverage of automated tests.
     */
    TESTING(
            "Testing",
            10,
            "Evaluate the automated test suite: coverage breadth, use of unit vs integration tests, "
            + "test naming conventions, and presence of edge-case tests for null and boundary inputs."
    );

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    /** Human-readable display name used in the JSON 'criterion' field. */
    private final String displayName;

    /** Maximum score (denominator) for this criterion. */
    private final int maxScore;

    /** Detailed evaluation guidance injected into the system prompt. */
    private final String evaluationGuidance;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    EvaluationCriterion(String displayName, int maxScore, String evaluationGuidance) {
        this.displayName       = displayName;
        this.maxScore          = maxScore;
        this.evaluationGuidance = evaluationGuidance;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** @return the display name used in the JSON output (e.g., {@code "Architecture"}). */
    public String getDisplayName()       { return displayName; }

    /** @return the maximum achievable score for this criterion. */
    public int    getMaxScore()          { return maxScore; }

    /** @return detailed guidance for the LLM on how to evaluate this criterion. */
    public String getEvaluationGuidance(){ return evaluationGuidance; }
}
