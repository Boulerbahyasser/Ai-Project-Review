package com.aireview.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Task 2: Mock Environment.
 *
 * <p><b>Testing philosophy:</b> All tests in this class are <em>hermetic</em> — they make zero
 * network calls. The {@link MockLLMProvider} acts as the test double for {@link LLMProvider},
 * validating:
 * <ul>
 *   <li>That the mock correctly dispatches to per-criterion JSON fixtures.</li>
 *   <li>That the JSON fixture strings are syntactically valid and can be fully parsed into
 *       {@link CriterionResult} objects via Jackson ({@code ObjectMapper}).</li>
 *   <li>That parsed {@link CriterionResult} objects pass the {@code isValid()} semantic check.</li>
 *   <li>That the failure simulation and call-count tracking work as specified.</li>
 * </ul>
 *
 * <p>This suite is the canonical demonstration that all JSON parsing logic is correct
 * <em>before</em> any real network provider is introduced in Tasks 3–5.
 */
@DisplayName("Task 2 – Mock Environment")
class MockLLMProviderTest {

    private MockLLMProvider mock;
    private ObjectMapper    mapper;

    @BeforeEach
    void setUp() {
        mock   = new MockLLMProvider();
        mapper = new ObjectMapper();
    }

    // =========================================================================
    // Section 1: Fixture routing — correct JSON returned per criterion keyword
    // =========================================================================

    @Nested
    @DisplayName("1. Fixture Routing")
    class FixtureRoutingTests {

        @Test
        @DisplayName("prompt containing 'architecture' routes to ARCHITECTURE_JSON")
        void architectureKeywordRoutes() throws LLMException {
            LLMRequest  req = LLMRequest.withDefaults("gemma2:2b",
                    "Evaluate the architecture of this project.");
            LLMResponse res = mock.call(req);

            assertTrue(res.rawContent().contains("\"Architecture\""),
                    "Response should contain the Architecture criterion.");
        }

        @Test
        @DisplayName("prompt containing 'SOLID' (uppercase) routes to SOLID_JSON")
        void solidKeywordCaseInsensitive() throws LLMException {
            LLMRequest  req = LLMRequest.withDefaults("gemma2:2b",
                    "Assess SOLID principles compliance.");
            LLMResponse res = mock.call(req);

            assertTrue(res.rawContent().contains("\"SOLID Principles\""));
        }

        @Test
        @DisplayName("prompt containing 'testing' routes to TESTING_JSON")
        void testingKeywordRoutes() throws LLMException {
            LLMRequest  req = LLMRequest.withDefaults("gemma2:2b",
                    "Review the testing strategy.");
            LLMResponse res = mock.call(req);

            assertTrue(res.rawContent().contains("\"Testing\""));
        }

        @Test
        @DisplayName("prompt containing 'test' (short form) routes to TESTING_JSON")
        void testShortKeywordRoutes() throws LLMException {
            LLMRequest  req = LLMRequest.withDefaults("gemma2:2b",
                    "Evaluate the test coverage.");
            LLMResponse res = mock.call(req);

            assertTrue(res.rawContent().contains("\"Testing\""));
        }

        @Test
        @DisplayName("unrecognised prompt routes to DEFAULT_JSON")
        void unrecognisedPromptReturnsDefault() throws LLMException {
            LLMRequest  req = LLMRequest.withDefaults("gemma2:2b",
                    "Give me a random evaluation.");
            LLMResponse res = mock.call(req);

            assertTrue(res.rawContent().contains("\"General\""),
                    "Unrecognised prompts should return the DEFAULT_JSON fixture.");
        }

        @ParameterizedTest(name = "prompt keyword ''{0}'' → criterion ''{1}''")
        @CsvSource({
            "architecture, Architecture",
            "solid,        SOLID Principles",
            "testing,      Testing",
            "test,         Testing"
        })
        @DisplayName("parameterized: all known keywords route to correct criterion")
        void allKeywordsRouteCorrectly(String keyword, String expectedCriterion)
                throws LLMException, Exception {
            LLMRequest req = LLMRequest.withDefaults("gemma2:2b",
                    "Please evaluate: " + keyword + " aspects.");
            LLMResponse res = mock.call(req);

            CriterionResult result = mapper.readValue(res.rawContent(), CriterionResult.class);
            assertEquals(expectedCriterion, result.criterion());
        }
    }

    // =========================================================================
    // Section 2: JSON fixture validity — all fixtures parse + pass isValid()
    // =========================================================================

    @Nested
    @DisplayName("2. JSON Fixture Validity")
    class FixtureValidityTests {

        @Test
        @DisplayName("ARCHITECTURE_JSON parses correctly")
        void architectureJsonParsesCorrectly() throws Exception {
            CriterionResult r = mapper.readValue(
                    MockLLMProvider.ARCHITECTURE_JSON, CriterionResult.class);
            assertEquals("Architecture", r.criterion());
            assertEquals(8,  r.score());
            assertEquals(10, r.maxScore());
            assertFalse(r.feedback().isBlank());
            assertEquals(1, r.issues().size());
        }

        @Test
        @DisplayName("SOLID_JSON parses correctly")
        void solidJsonParsesCorrectly() throws Exception {
            CriterionResult r = mapper.readValue(
                    MockLLMProvider.SOLID_JSON, CriterionResult.class);
            assertEquals("SOLID Principles", r.criterion());
            assertEquals(7,  r.score());
            assertEquals(10, r.maxScore());
            assertEquals(2,  r.issues().size());
        }

        @Test
        @DisplayName("TESTING_JSON parses correctly")
        void testingJsonParsesCorrectly() throws Exception {
            CriterionResult r = mapper.readValue(
                    MockLLMProvider.TESTING_JSON, CriterionResult.class);
            assertEquals("Testing", r.criterion());
            assertEquals(6,  r.score());
            assertEquals(10, r.maxScore());
            assertEquals(3,  r.issues().size());
        }

        @Test
        @DisplayName("DEFAULT_JSON parses correctly with empty issues list")
        void defaultJsonParsesCorrectly() throws Exception {
            CriterionResult r = mapper.readValue(
                    MockLLMProvider.DEFAULT_JSON, CriterionResult.class);
            assertEquals("General", r.criterion());
            assertEquals(5,  r.score());
            assertEquals(10, r.maxScore());
            assertTrue(r.issues().isEmpty());
        }

        @Test
        @DisplayName("isValid() returns true for ARCHITECTURE_JSON")
        void architectureIsValid() throws Exception {
            assertTrue(mapper.readValue(MockLLMProvider.ARCHITECTURE_JSON,
                    CriterionResult.class).isValid());
        }

        @Test
        @DisplayName("isValid() returns true for SOLID_JSON")
        void solidIsValid() throws Exception {
            assertTrue(mapper.readValue(MockLLMProvider.SOLID_JSON,
                    CriterionResult.class).isValid());
        }

        @Test
        @DisplayName("isValid() returns true for TESTING_JSON")
        void testingIsValid() throws Exception {
            assertTrue(mapper.readValue(MockLLMProvider.TESTING_JSON,
                    CriterionResult.class).isValid());
        }

        @Test
        @DisplayName("isValid() returns true for DEFAULT_JSON")
        void defaultIsValid() throws Exception {
            assertTrue(mapper.readValue(MockLLMProvider.DEFAULT_JSON,
                    CriterionResult.class).isValid());
        }

        @Test
        @DisplayName("all fixture rawContent strings pass looksLikeJson()")
        void allFixturesLookLikeJson() throws LLMException {
            String[] prompts = {"architecture", "solid", "testing", "unknown-criterion"};
            for (String keyword : prompts) {
                LLMResponse res = mock.call(
                        LLMRequest.withDefaults("gemma2:2b", keyword));
                assertTrue(res.looksLikeJson(),
                        "Fixture for keyword '" + keyword + "' must pass looksLikeJson().");
            }
        }

        @Test
        @DisplayName("issues list returned from mock is immutable (defensive copy)")
        void issuesListIsImmutable() throws Exception {
            CriterionResult r = mapper.readValue(
                    MockLLMProvider.ARCHITECTURE_JSON, CriterionResult.class);
            List<String> issues = r.issues();
            assertThrows(UnsupportedOperationException.class, () -> issues.add("hack"));
        }
    }

    // =========================================================================
    // Section 3: Full round-trip — call → parse → validate
    // =========================================================================

    @Nested
    @DisplayName("3. Full Round-Trip (call → parse → validate)")
    class RoundTripTests {

        @Test
        @DisplayName("round-trip for Architecture: call → parse → isValid()")
        void architectureRoundTrip() throws Exception {
            LLMResponse res = mock.call(
                    LLMRequest.withDefaults("gemma2:2b", "Evaluate the architecture."));
            CriterionResult result = mapper.readValue(res.rawContent(), CriterionResult.class);
            assertTrue(result.isValid(), "Parsed result must pass isValid().");
            assertTrue(result.scorePercent() >= 0.0 && result.scorePercent() <= 100.0);
        }

        @Test
        @DisplayName("round-trip for SOLID: call → parse → score in [0, maxScore]")
        void solidRoundTrip() throws Exception {
            LLMResponse res = mock.call(
                    LLMRequest.withDefaults("gemma2:2b", "Check solid compliance."));
            CriterionResult result = mapper.readValue(res.rawContent(), CriterionResult.class);
            assertTrue(result.score() >= 0 && result.score() <= result.maxScore());
        }

        @Test
        @DisplayName("response modelUsed matches requested model")
        void responseModelMatchesRequest() throws LLMException {
            LLMRequest req = LLMRequest.withDefaults("gemma2:2b", "Evaluate architecture.");
            LLMResponse res = mock.call(req);
            assertEquals(req.model(), res.modelUsed());
        }

        @Test
        @DisplayName("response durationMillis matches configured simulated duration")
        void responseDurationMatchesConfigured() throws LLMException {
            MockLLMProvider timedMock = new MockLLMProvider(123L);
            LLMResponse res = timedMock.call(
                    LLMRequest.withDefaults("gemma2:2b", "Evaluate architecture."));
            assertEquals(123L, res.durationMillis());
        }
    }

    // =========================================================================
    // Section 4: Failure simulation & call tracking
    // =========================================================================

    @Nested
    @DisplayName("4. Failure Simulation & Call Tracking")
    class FailureSimulationTests {

        @Test
        @DisplayName("setFailOnNextCall(true) causes next call to throw LLMException")
        void failOnNextCallThrows() {
            mock.setFailOnNextCall(true);
            assertThrows(LLMException.class,
                    () -> mock.call(LLMRequest.withDefaults("gemma2:2b", "Evaluate.")));
        }

        @Test
        @DisplayName("after failure, subsequent call succeeds (single-shot reset)")
        void failureIsSingleShot() throws LLMException {
            mock.setFailOnNextCall(true);

            // First call fails.
            assertThrows(LLMException.class,
                    () -> mock.call(LLMRequest.withDefaults("gemma2:2b", "Evaluate.")));

            // Second call must succeed — flag was reset.
            assertDoesNotThrow(
                    () -> mock.call(LLMRequest.withDefaults("gemma2:2b", "Evaluate.")));
        }

        @Test
        @DisplayName("callCount increments correctly across multiple calls")
        void callCountIncrements() throws LLMException {
            assertEquals(0, mock.getCallCount());
            mock.call(LLMRequest.withDefaults("gemma2:2b", "prompt 1"));
            assertEquals(1, mock.getCallCount());
            mock.call(LLMRequest.withDefaults("gemma2:2b", "prompt 2"));
            assertEquals(2, mock.getCallCount());
        }

        @Test
        @DisplayName("callCount increments even on failure")
        void callCountIncrementsOnFailure() {
            mock.setFailOnNextCall(true);
            assertThrows(LLMException.class,
                    () -> mock.call(LLMRequest.withDefaults("gemma2:2b", "prompt")));
            assertEquals(1, mock.getCallCount(), "Failed call must still increment the counter.");
        }

        @Test
        @DisplayName("getLastRequest() returns the most recently received request")
        void lastRequestTracked() throws LLMException {
            assertNull(mock.getLastRequest(), "Initially should be null.");

            LLMRequest req = LLMRequest.withDefaults("gemma2:2b", "Evaluate solid.");
            mock.call(req);
            assertSame(req, mock.getLastRequest());
        }

        @Test
        @DisplayName("reset() clears callCount, lastRequest, and failOnNextCall")
        void resetClearsState() throws LLMException {
            mock.call(LLMRequest.withDefaults("gemma2:2b", "prompt"));
            mock.setFailOnNextCall(true);

            mock.reset();

            assertEquals(0, mock.getCallCount());
            assertNull(mock.getLastRequest());
            // After reset, next call must NOT throw (failOnNextCall was cleared).
            assertDoesNotThrow(
                    () -> mock.call(LLMRequest.withDefaults("gemma2:2b", "prompt")));
        }

        @Test
        @DisplayName("constructor rejects negative simulatedDurationMillis")
        void constructorRejectsNegativeDuration() {
            assertThrows(IllegalArgumentException.class, () -> new MockLLMProvider(-1L));
        }
    }
}
