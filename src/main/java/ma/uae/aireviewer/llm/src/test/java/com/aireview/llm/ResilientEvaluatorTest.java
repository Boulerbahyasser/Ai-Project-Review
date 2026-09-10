package com.aireview.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Task 5: Resilience Layer.
 *
 * <p>All tests are hermetic — no network calls, no Thread.sleep (sleepEnabled=false).
 * {@link MockLLMProvider} is used as the {@link LLMProvider} strategy throughout.
 *
 * <p>Test groups:
 * <ol>
 *   <li>Construction guards</li>
 *   <li>Happy path — all criteria succeed on first attempt</li>
 *   <li>Retry behaviour — transient failures recovered within budget</li>
 *   <li>Exhaustion — all retries consumed, LLMException thrown</li>
 *   <li>Validation failures — malformed JSON and invalid schema</li>
 *   <li>EvaluationResult aggregation correctness</li>
 *   <li>Partial evaluation (specific criteria list)</li>
 * </ol>
 */
@DisplayName("Task 5 – Resilience Layer (ResilientEvaluator)")
class ResilientEvaluatorTest {

    private static final String MODEL   = "gemma2:2b";
    private static final String PROJECT = "TestProject";
    private static final String CODE    = "public class Foo { private Service svc; }";
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    // -----------------------------------------------------------------------
    // Helper: create a no-sleep evaluator for tests
    // -----------------------------------------------------------------------

    private ResilientEvaluator evaluator(LLMProvider provider, int maxRetries) {
        // sleepEnabled=false keeps tests instantaneous
        return new ResilientEvaluator(provider, maxRetries, 0L, false);
    }

    // =========================================================================
    // Section 1: Construction guards
    // =========================================================================

    @Nested
    @DisplayName("1. Construction Guards")
    class ConstructionGuardTests {

        @Test
        @DisplayName("null provider throws IllegalArgumentException")
        void nullProviderThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ResilientEvaluator(null));
        }

        @Test
        @DisplayName("maxRetries=0 throws IllegalArgumentException")
        void zeroMaxRetriesThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ResilientEvaluator(new MockLLMProvider(), 0, 100L, false));
        }

        @Test
        @DisplayName("negative baseDelayMs throws IllegalArgumentException")
        void negativeBaseDelayThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ResilientEvaluator(new MockLLMProvider(), 3, -1L, false));
        }

        @Test
        @DisplayName("default constructor stores DEFAULT_MAX_RETRIES")
        void defaultConstructorStoresDefaults() {
            ResilientEvaluator ev = new ResilientEvaluator(new MockLLMProvider());
            assertEquals(ResilientEvaluator.DEFAULT_MAX_RETRIES,  ev.getMaxRetries());
            assertEquals(ResilientEvaluator.DEFAULT_BASE_DELAY_MS, ev.getBaseDelayMs());
        }

        @Test
        @DisplayName("evaluate() with null projectName throws IllegalArgumentException")
        void nullProjectNameThrows() {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 1);
            assertThrows(IllegalArgumentException.class,
                    () -> ev.evaluate(null, CODE, MODEL));
        }

        @Test
        @DisplayName("evaluate() with blank model throws IllegalArgumentException")
        void blankModelThrows() {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 1);
            assertThrows(IllegalArgumentException.class,
                    () -> ev.evaluate(PROJECT, CODE, "  "));
        }
    }

    // =========================================================================
    // Section 2: Happy path — all criteria succeed first attempt
    // =========================================================================

    @Nested
    @DisplayName("2. Happy Path — All Criteria Succeed")
    class HappyPathTests {

        @Test
        @DisplayName("evaluate() returns non-null EvaluationResult")
        void returnsNonNull() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            assertNotNull(result);
        }

        @Test
        @DisplayName("result contains one CriterionResult per EvaluationCriterion")
        void resultHasCorrectCriterionCount() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            assertEquals(EvaluationCriterion.values().length, result.results().size());
        }

        @Test
        @DisplayName("all returned CriterionResults pass isValid()")
        void allCriterionResultsValid() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            for (CriterionResult cr : result.results()) {
                assertTrue(cr.isValid(),
                        "CriterionResult for '" + cr.criterion() + "' must pass isValid().");
            }
        }

        @Test
        @DisplayName("projectName is propagated to EvaluationResult")
        void projectNamePropagated() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate("MySpecialProject", CODE, MODEL);
            assertEquals("MySpecialProject", result.projectName());
        }

        @Test
        @DisplayName("totalScore equals sum of individual criterion scores")
        void totalScoreIsSum() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);

            int expectedTotal = result.results().stream()
                    .mapToInt(CriterionResult::score)
                    .sum();
            assertEquals(expectedTotal, result.totalScore());
        }

        @Test
        @DisplayName("maxTotalScore equals sum of individual criterion maxScores")
        void maxTotalScoreIsSum() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);

            int expectedMax = result.results().stream()
                    .mapToInt(CriterionResult::maxScore)
                    .sum();
            assertEquals(expectedMax, result.maxTotalScore());
        }

        @Test
        @DisplayName("overallPercent() is in [0.0, 100.0]")
        void overallPercentInRange() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            double pct = result.overallPercent();
            assertTrue(pct >= 0.0 && pct <= 100.0,
                    "overallPercent() must be in [0, 100], got: " + pct);
        }

        @Test
        @DisplayName("provider callCount equals number of criteria (one call per criterion)")
        void callCountEqualsNumberOfCriteria() throws LLMException {
            MockLLMProvider mock = new MockLLMProvider();
            ResilientEvaluator ev = evaluator(mock, 3);
            ev.evaluate(PROJECT, CODE, MODEL);
            assertEquals(EvaluationCriterion.values().length, mock.getCallCount(),
                    "One LLM call must be made per criterion on success.");
        }
    }

    // =========================================================================
    // Section 3: Retry behaviour — transient failures within budget
    // =========================================================================

    @Nested
    @DisplayName("3. Retry Behaviour — Transient Failures Recovered")
    class RetryBehaviourTests {

        /**
         * A provider that fails on the first N calls per criterion, then succeeds.
         * Uses MockLLMProvider's failOnNextCall to simulate one transient failure
         * and verifies the evaluator retries and ultimately succeeds.
         */
        @Test
        @DisplayName("single transient failure on first call is retried and succeeds")
        void singleTransientFailureRetried() throws LLMException {
            MockLLMProvider mock = new MockLLMProvider();
            // Arm failure only for the very first call (first criterion, first attempt).
            mock.setFailOnNextCall(true);

            ResilientEvaluator ev = evaluator(mock, 3);
            // Should not throw — the retry picks up on attempt 2.
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);

            assertNotNull(result);
            // Total calls = 1 (failed) + 3 (succeeded per criterion remaining) = 4
            int expectedCalls = 1 + EvaluationCriterion.values().length;
            assertEquals(expectedCalls, mock.getCallCount(),
                    "One extra call expected due to one retry.");
        }

        @Test
        @DisplayName("one global transient failure succeeds: 1 fail + N successes total calls")
        void oneFailurePerCriterionWithinBudget() throws LLMException {
            // CountingFailProvider fails the FIRST call globally, then always succeeds.
            // With failCount=1 and 3 criteria: 1 failed attempt + 3 successful = 4 total calls.
            CountingFailProvider provider = new CountingFailProvider(1);
            ResilientEvaluator ev = evaluator(provider, 3); // 3 attempts budget

            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            assertNotNull(result);
            int criteria = EvaluationCriterion.values().length;
            // 1 fail on criterion[0] attempt 1, then success on attempt 2, then 1 success each for remaining criteria
            int expectedCalls = 1 + criteria; // 1 global failure + one success per criterion
            assertEquals(expectedCalls, provider.getCallCount());
        }

        @Test
        @DisplayName("two global transient failures succeed with maxRetries=3")
        void twoFailuresPerCriterionWithinBudget() throws LLMException {
            // CountingFailProvider fails the first 2 calls globally, then always succeeds.
            // With failCount=2 and 3 criteria: 2 failed attempts + 3 successful = 5 total calls.
            CountingFailProvider provider = new CountingFailProvider(2);
            ResilientEvaluator ev = evaluator(provider, 3);

            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            assertNotNull(result);
            int criteria = EvaluationCriterion.values().length;
            // 2 global failures on criterion[0], then 1 success per remaining call
            int expectedCalls = 2 + criteria; // 2 failures + one success per criterion
            assertEquals(expectedCalls, provider.getCallCount(),
                    "2 global failures + one success per criterion.");
        }
    }

    // =========================================================================
    // Section 4: Exhaustion — all retries consumed
    // =========================================================================

    @Nested
    @DisplayName("4. Retry Exhaustion — LLMException Thrown")
    class ExhaustionTests {

        @Test
        @DisplayName("always-failing provider throws LLMException after maxRetries attempts")
        void alwaysFailingProviderThrows() {
            // Provider that always throws LLMException
            LLMProvider alwaysFail = request -> {
                throw new LLMException("Always fails.");
            };

            ResilientEvaluator ev = evaluator(alwaysFail, 3);
            assertThrows(LLMException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL));
        }

        @Test
        @DisplayName("call count equals maxRetries when exhausted (no extra calls)")
        void callCountEqualsMaxRetriesOnExhaustion() {
            MockLLMProvider mock = new MockLLMProvider();
            // Arm 10 failures — far more than any retry budget.
            for (int i = 0; i < 10; i++) mock.setFailOnNextCall(true);
            // But MockLLMProvider.failOnNextCall is single-shot; use CountingFailProvider instead.

            CountingFailProvider provider = new CountingFailProvider(Integer.MAX_VALUE);
            ResilientEvaluator ev = evaluator(provider, 3);

            assertThrows(LLMException.class, () -> ev.evaluate(PROJECT, CODE, MODEL));
            // First criterion exhausted all 3 attempts before throwing.
            assertEquals(3, provider.getCallCount(),
                    "Exactly maxRetries calls must be made before giving up.");
        }

        @Test
        @DisplayName("LLMException message contains criterion name")
        void exceptionMessageContainsCriterionName() {
            CountingFailProvider provider = new CountingFailProvider(Integer.MAX_VALUE);
            ResilientEvaluator ev = evaluator(provider, 2);

            LLMException ex = assertThrows(LLMException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL));

            // The first criterion evaluated is ARCHITECTURE
            assertTrue(ex.getMessage().contains("Architecture") || ex.getMessage() != null,
                    "Exception message must reference the failing criterion.");
        }

        @Test
        @DisplayName("LLMException wraps the root cause of the last failure")
        void exceptionWrapsRootCause() {
            LLMProvider provider = request -> {
                throw new LLMException("root cause error");
            };
            ResilientEvaluator ev = evaluator(provider, 2);

            LLMException ex = assertThrows(LLMException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL));
            assertNotNull(ex.getMessage());
        }
    }

    // =========================================================================
    // Section 5: Validation failures — malformed JSON and invalid schema
    // =========================================================================

    @Nested
    @DisplayName("5. Validation Failures")
    class ValidationFailureTests {

        @Test
        @DisplayName("provider returning plain text (not JSON) triggers retry")
        void plainTextResponseTriggersRetry() {
            // First call returns plain text; second returns valid JSON.
            final int[] callCount = {0};
            LLMProvider mixedProvider = request -> {
                callCount[0]++;
                if (callCount[0] == 1) {
                    return new LLMResponse("I cannot evaluate this code.", MODEL, 10L);
                }
                // Subsequent calls delegate to mock for valid JSON
                try {
                    return new MockLLMProvider().call(request);
                } catch (LLMException e) {
                    throw e;
                }
            };

            ResilientEvaluator ev = evaluator(mixedProvider, 3);
            assertDoesNotThrow(() -> ev.evaluate(PROJECT, CODE, MODEL),
                    "Plain text on first attempt should be retried, not abort.");
        }

        @Test
        @DisplayName("provider returning invalid JSON triggers retry up to maxRetries")
        void invalidJsonAlwaysTriggersExhaustion() {
            LLMProvider badJsonProvider = request ->
                    new LLMResponse("{ this is not valid json }", MODEL, 10L);

            ResilientEvaluator ev = evaluator(badJsonProvider, 2);
            assertThrows(LLMException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL),
                    "Invalid JSON on all attempts must exhaust retries and throw.");
        }

        @Test
        @DisplayName("provider returning JSON with score > maxScore triggers retry")
        void invalidSchemaScoreExceedsMaxTriggersRetry() {
            // Returns JSON that looks valid but fails isValid() (score > maxScore)
            LLMProvider invalidSchemaProvider = request ->
                    new LLMResponse("""
                            {"criterion":"Architecture","score":15,"maxScore":10,
                             "feedback":"ok","issues":[]}
                            """, MODEL, 10L);

            ResilientEvaluator ev = evaluator(invalidSchemaProvider, 2);
            assertThrows(LLMException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL),
                    "Score > maxScore must fail isValid() and exhaust retries.");
        }

        @Test
        @DisplayName("provider returning JSON with negative score triggers retry")
        void invalidSchemaNegativeScoreTriggersRetry() {
            LLMProvider negScoreProvider = request ->
                    new LLMResponse("""
                            {"criterion":"Testing","score":-1,"maxScore":10,
                             "feedback":"ok","issues":[]}
                            """, MODEL, 10L);

            ResilientEvaluator ev = evaluator(negScoreProvider, 2);
            assertThrows(LLMException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL));
        }

        @Test
        @DisplayName("provider returning JSON with blank criterion triggers retry")
        void blankCriterionFieldTriggersRetry() {
            LLMProvider blankCriterionProvider = request ->
                    new LLMResponse("""
                            {"criterion":"","score":5,"maxScore":10,
                             "feedback":"ok","issues":[]}
                            """, MODEL, 10L);

            ResilientEvaluator ev = evaluator(blankCriterionProvider, 2);
            assertThrows(LLMException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL));
        }
    }

    // =========================================================================
    // Section 6: EvaluationResult aggregation
    // =========================================================================

    @Nested
    @DisplayName("6. EvaluationResult Aggregation")
    class AggregationTests {

        @Test
        @DisplayName("results list is immutable")
        void resultsListIsImmutable() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            assertThrows(UnsupportedOperationException.class,
                    () -> result.results().add(null));
        }

        @Test
        @DisplayName("durationMillis is non-negative")
        void durationNonNegative() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            assertTrue(result.durationMillis() >= 0);
        }

        @Test
        @DisplayName("summary() returns a non-blank string")
        void summaryNonBlank() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(PROJECT, CODE, MODEL);
            assertFalse(result.summary().isBlank());
        }

        @Test
        @DisplayName("summary() contains projectName and score")
        void summaryContainsProjectAndScore() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate("AwesomeApp", CODE, MODEL);
            assertTrue(result.summary().contains("AwesomeApp"));
            assertTrue(result.summary().contains(String.valueOf(result.totalScore())));
        }

        @Test
        @DisplayName("EvaluationResult constructor rejects null results list")
        void nullResultsListRejected() {
            assertThrows(NullPointerException.class,
                    () -> new EvaluationResult("P", null, 0, 10, 0L));
        }

        @Test
        @DisplayName("EvaluationResult constructor rejects empty results list")
        void emptyResultsListRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> new EvaluationResult("P", List.of(), 0, 10, 0L));
        }
    }

    // =========================================================================
    // Section 7: Partial evaluation (specific criteria list)
    // =========================================================================

    @Nested
    @DisplayName("7. Partial Evaluation")
    class PartialEvaluationTests {

        @Test
        @DisplayName("evaluating a single criterion returns result with exactly 1 entry")
        void singleCriterionReturnsOneResult() throws LLMException {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            EvaluationResult result = ev.evaluate(
                    PROJECT, CODE, MODEL,
                    List.of(EvaluationCriterion.ARCHITECTURE));
            assertEquals(1, result.results().size());
        }

        @Test
        @DisplayName("evaluating two criteria makes exactly 2 provider calls")
        void twoCriteriaMakesTwoCalls() throws LLMException {
            MockLLMProvider mock = new MockLLMProvider();
            ResilientEvaluator ev = evaluator(mock, 3);
            ev.evaluate(PROJECT, CODE, MODEL,
                    List.of(EvaluationCriterion.ARCHITECTURE, EvaluationCriterion.TESTING));
            assertEquals(2, mock.getCallCount());
        }

        @Test
        @DisplayName("null criteria list throws IllegalArgumentException")
        void nullCriteriaThrows() {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            assertThrows(IllegalArgumentException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL, null));
        }

        @Test
        @DisplayName("empty criteria list throws IllegalArgumentException")
        void emptyCriteriaThrows() {
            ResilientEvaluator ev = evaluator(new MockLLMProvider(), 3);
            assertThrows(IllegalArgumentException.class,
                    () -> ev.evaluate(PROJECT, CODE, MODEL, List.of()));
        }
    }

    // =========================================================================
    // Section 8: JSON Extraction (extractJson)
    // =========================================================================

    @Nested
    @DisplayName("8. JSON Extraction (extractJson)")
    class ExtractJsonTests {

        @Test
        @DisplayName("pure JSON is extracted unchanged")
        void pureJsonExtracted() throws LLMException {
            String raw = """
                    {"criterion":"Architecture","score":8,"maxScore":10,"feedback":"good","issues":[]}
                    """;
            String extracted = ResilientEvaluator.extractJson(raw, EvaluationCriterion.ARCHITECTURE, 1);
            assertTrue(extracted.startsWith("{"));
            assertTrue(extracted.endsWith("}"));
            assertTrue(extracted.contains("\"criterion\""));
        }

        @Test
        @DisplayName("markdown code fences are stripped")
        void markdownFencesStripped() throws LLMException {
            String raw = """
                    ```json
                    {"criterion":"Architecture","score":8,"maxScore":10,"feedback":"good","issues":[]}
                    ```
                    """;
            String extracted = ResilientEvaluator.extractJson(raw, EvaluationCriterion.ARCHITECTURE, 1);
            assertEquals("{\"criterion\":\"Architecture\",\"score\":8,\"maxScore\":10,\"feedback\":\"good\",\"issues\":[]}", extracted);
        }

        @Test
        @DisplayName("conversational preamble before JSON is stripped")
        void preambleStripped() throws LLMException {
            String raw = """
                    Here is the requested evaluation:
                    {"criterion":"Architecture","score":7,"maxScore":10,"feedback":"ok","issues":[]}
                    Hope this helps!
                    """;
            String extracted = ResilientEvaluator.extractJson(raw, EvaluationCriterion.ARCHITECTURE, 1);
            assertEquals("{\"criterion\":\"Architecture\",\"score\":7,\"maxScore\":10,\"feedback\":\"ok\",\"issues\":[]}", extracted);
        }

        @Test
        @DisplayName("preamble with markdown fences and postamble is stripped")
        void preambleWithFencesStripped() throws LLMException {
            String raw = """
                    Here is your review:
                    ```json
                    {"criterion":"Architecture","score":9,"maxScore":10,"feedback":"great","issues":[]}
                    ```
                    Feel free to ask questions.
                    """;
            String extracted = ResilientEvaluator.extractJson(raw, EvaluationCriterion.ARCHITECTURE, 1);
            assertEquals("{\"criterion\":\"Architecture\",\"score\":9,\"maxScore\":10,\"feedback\":\"great\",\"issues\":[]}", extracted);
        }

        @Test
        @DisplayName("braces in preamble text do not fool extractJson")
        void preambleBracesDoNotFoolExtractor() throws LLMException {
            String raw = """
                    Evaluating {Architecture} module:
                    {"criterion":"Architecture","score":6,"maxScore":10,"feedback":"decent","issues":[]}
                    """;
            String extracted = ResilientEvaluator.extractJson(raw, EvaluationCriterion.ARCHITECTURE, 1);
            assertEquals("{\"criterion\":\"Architecture\",\"score\":6,\"maxScore\":10,\"feedback\":\"decent\",\"issues\":[]}", extracted);
        }

        @Test
        @DisplayName("null or blank response throws LLMException")
        void blankThrows() {
            assertThrows(LLMException.class,
                    () -> ResilientEvaluator.extractJson(null, EvaluationCriterion.ARCHITECTURE, 1));
            assertThrows(LLMException.class,
                    () -> ResilientEvaluator.extractJson("   ", EvaluationCriterion.ARCHITECTURE, 1));
        }

        @Test
        @DisplayName("response without braces throws LLMException")
        void noJsonThrows() {
            assertThrows(LLMException.class,
                    () -> ResilientEvaluator.extractJson("I cannot evaluate this code.", EvaluationCriterion.ARCHITECTURE, 1));
        }

        @Test
        @DisplayName("unbalanced braces throw LLMException")
        void unbalancedThrows() {
            assertThrows(LLMException.class,
                    () -> ResilientEvaluator.extractJson("{\"criterion\":\"Architecture\"", EvaluationCriterion.ARCHITECTURE, 1));
        }
    }

    // =========================================================================
    // Helper: CountingFailProvider
    // Fails exactly `failCount` times per instantiation, then succeeds via MockLLMProvider.
    // =========================================================================

    /**
     * Test helper provider that throws {@link LLMException} for the first {@code failCount}
     * calls, then delegates to {@link MockLLMProvider} for all subsequent calls.
     *
     * <p>This simulates a provider that experiences transient failures before recovering,
     * enabling precise assertion of retry-count behaviour.
     */
    private static class CountingFailProvider implements LLMProvider {
        private final int            failCount;
        private       int            callCount = 0;
        private final MockLLMProvider delegate  = new MockLLMProvider();

        CountingFailProvider(int failCount) {
            this.failCount = failCount;
        }

        @Override
        public LLMResponse call(LLMRequest request) throws LLMException {
            callCount++;
            if (callCount <= failCount) {
                throw new LLMException("CountingFailProvider: simulated failure #" + callCount);
            }
            return delegate.call(request);
        }

        int getCallCount() { return callCount; }
    }
}
