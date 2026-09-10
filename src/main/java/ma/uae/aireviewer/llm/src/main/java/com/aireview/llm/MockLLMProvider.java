package com.aireview.llm;

import java.util.Map;

/**
 * PATTERN: Strategy (Concrete Implementation) + Adapter
 *
 * <p>{@code MockLLMProvider} is a concrete implementation of the {@link LLMProvider} Strategy
 * interface. It serves as the primary testing double for the entire LLM subsystem, enabling
 * deterministic, fast, and network-free testing of all layers that depend on an LLM.
 *
 * <p><b>Strategy role:</b> This class is injected wherever an {@link LLMProvider} is needed
 * during tests. The calling code (e.g., {@code ResilientEvaluator}) is completely unaware that
 * it is talking to a mock rather than a real model — the interface contract is identical.
 *
 * <p><b>Adapter role:</b> Rather than adapting an external HTTP API, this class "adapts" a set
 * of in-memory fixture strings to the {@link LLMProvider} contract. This keeps all test fixtures
 * in one place, avoiding scattered hard-coded JSON across test files.
 *
 * <p><b>Design decisions:</b>
 * <ul>
 *   <li>Responses are keyed by criterion keyword (case-insensitive substring match against the
 *       prompt), so the mock can serve different criteria without a complex dispatch mechanism.
 *   <li>A {@code DEFAULT} entry ensures the mock never returns null, even for unrecognised prompts.
 *   <li>A configurable {@code failOnNextCall} flag simulates provider failures, allowing the
 *       resilience layer (Task 5) to be tested against realistic error conditions.
 *   <li>Call count tracking enables tests to assert retry behaviour precisely.
 * </ul>
 *
 * <p><b>Security note:</b> The mock does not validate, sanitise, or log the prompt — consistent
 * with the contract that prompts may contain sensitive source-code snippets.
 */
public class MockLLMProvider implements LLMProvider {

    // -----------------------------------------------------------------------
    // Hardcoded valid JSON fixtures — one per evaluation criterion.
    // These strings must comply with the CriterionResult schema at all times.
    // -----------------------------------------------------------------------

    /** Valid JSON response for the "Architecture" criterion. */
    public static final String ARCHITECTURE_JSON = """
            {
              "criterion": "Architecture",
              "score":     8,
              "maxScore":  10,
              "feedback":  "Good layered structure with clear package boundaries. \
            Controller layer correctly delegates to the service layer. \
            Minor coupling found between the repository and the DTO layer.",
              "issues":    [
                "Repository layer directly references DTO classes — consider a mapper."
              ]
            }
            """;

    /** Valid JSON response for the "SOLID Principles" criterion. */
    public static final String SOLID_JSON = """
            {
              "criterion": "SOLID Principles",
              "score":     7,
              "maxScore":  10,
              "feedback":  "Single Responsibility and Open/Closed are well observed. \
            Dependency Inversion is violated in the ServiceLocator class, which \
            directly instantiates concrete dependencies.",
              "issues":    [
                "ServiceLocator violates DIP — inject dependencies via constructor.",
                "UserService has more than one reason to change (handles both auth and profile)."
              ]
            }
            """;

    /** Valid JSON response for the "Testing" criterion. */
    public static final String TESTING_JSON = """
            {
              "criterion": "Testing",
              "score":     6,
              "maxScore":  10,
              "feedback":  "Unit tests present for core service layer. \
            Integration tests and edge-case coverage are missing. \
            Test naming does not follow the given/when/then convention.",
              "issues":    [
                "No integration tests found.",
                "Missing edge-case tests for null inputs in UserService.",
                "Test class names do not indicate the system-under-test clearly."
              ]
            }
            """;

    /** Default JSON returned when no criterion keyword matches the prompt. */
    public static final String DEFAULT_JSON = """
            {
              "criterion": "General",
              "score":     5,
              "maxScore":  10,
              "feedback":  "General code quality is acceptable. \
            No specific criterion could be matched from the prompt.",
              "issues":    []
            }
            """;

    // -----------------------------------------------------------------------
    // Criterion → fixture mapping (keyword → JSON string).
    // Keys are lowercase for case-insensitive matching.
    // -----------------------------------------------------------------------
    private static final Map<String, String> FIXTURE_MAP = Map.of(
            "architecture", ARCHITECTURE_JSON,
            "solid",        SOLID_JSON,
            "testing",      TESTING_JSON,
            "test",         TESTING_JSON   // allow "testing" or "test" as keywords
    );

    // -----------------------------------------------------------------------
    // State for test inspection
    // -----------------------------------------------------------------------

    /** Number of times {@link #call(LLMRequest)} has been invoked. */
    private int callCount = 0;

    /** The last request received (for assertion in tests). */
    private LLMRequest lastRequest = null;

    /**
     * When {@code true}, the next call to {@link #call} will throw an {@link LLMException}
     * simulating a provider failure. Automatically resets to {@code false} after firing.
     */
    private boolean failOnNextCall = false;

    /** Simulated latency in milliseconds reported in the response (not a real sleep). */
    private final long simulatedDurationMillis;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Creates a {@code MockLLMProvider} with a default simulated duration of 50 ms.
     */
    public MockLLMProvider() {
        this(50L);
    }

    /**
     * Creates a {@code MockLLMProvider} with a custom simulated duration.
     *
     * @param simulatedDurationMillis the value to report in {@link LLMResponse#durationMillis()}
     */
    public MockLLMProvider(long simulatedDurationMillis) {
        if (simulatedDurationMillis < 0) {
            throw new IllegalArgumentException(
                    "MockLLMProvider: simulatedDurationMillis must be non-negative.");
        }
        this.simulatedDurationMillis = simulatedDurationMillis;
    }

    // -----------------------------------------------------------------------
    // LLMProvider implementation
    // -----------------------------------------------------------------------

    /**
     * Returns a hardcoded {@link LLMResponse} based on criterion keywords in the prompt.
     *
     * <p>If {@link #setFailOnNextCall(boolean)} has been called with {@code true}, this method
     * throws an {@link LLMException} and resets the flag, simulating a transient failure.
     *
     * <p>Criterion detection uses a case-insensitive substring search of the prompt against
     * the keys in {@link #FIXTURE_MAP}. The first matching key wins.
     *
     * @param request the request; never null
     * @return a deterministic {@link LLMResponse}
     * @throws LLMException if {@code failOnNextCall} is {@code true}
     */
    @Override
    public LLMResponse call(LLMRequest request) throws LLMException {
        callCount++;
        lastRequest = request;

        // Simulate provider failure (for resilience testing in Task 5).
        if (failOnNextCall) {
            failOnNextCall = false; // reset after firing (single-shot)
            throw new LLMException("MockLLMProvider: simulated provider failure (call #" + callCount + ").");
        }

        // Select fixture by scanning the prompt for criterion keywords.
        String promptLower = request.prompt().toLowerCase();
        String responseJson = DEFAULT_JSON;

        for (Map.Entry<String, String> entry : FIXTURE_MAP.entrySet()) {
            if (promptLower.contains(entry.getKey())) {
                responseJson = entry.getValue();
                break;
            }
        }

        return new LLMResponse(responseJson, request.model(), simulatedDurationMillis);
    }

    // -----------------------------------------------------------------------
    // Test-support accessors (not part of the LLMProvider interface)
    // -----------------------------------------------------------------------

    /**
     * Returns the total number of times {@link #call(LLMRequest)} has been invoked.
     * Used by tests to assert retry counts precisely.
     *
     * @return call count since construction or last {@link #reset()}
     */
    public int getCallCount() {
        return callCount;
    }

    /**
     * Returns the last {@link LLMRequest} received by this mock, or {@code null} if
     * {@link #call} has never been invoked.
     *
     * @return last request, or null
     */
    public LLMRequest getLastRequest() {
        return lastRequest;
    }

    /**
     * Configures whether the next call should throw an {@link LLMException}.
     *
     * <p>The flag is single-shot: it resets to {@code false} after one failure is triggered.
     * Call this method again to schedule another failure.
     *
     * @param fail {@code true} to arm the failure trigger
     */
    public void setFailOnNextCall(boolean fail) {
        this.failOnNextCall = fail;
    }

    /**
     * Resets call count, last request, and failure flag to their initial state.
     * Useful for re-using the same mock instance across multiple test methods.
     */
    public void reset() {
        callCount      = 0;
        lastRequest    = null;
        failOnNextCall = false;
    }
}
