package com.aireview.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Task 1: Core Interfaces & DTOs.
 *
 * <p>All tests in this class operate in-memory with no network calls. The {@code MockLLMProvider}
 * (scaffolded inline here for Task 1, fully implemented in Task 2) is used to exercise the
 * {@link LLMProvider} contract without any real HTTP dependency.
 *
 * <p>Test structure follows the Nested class pattern to group related concerns, improving
 * readability of the test report.
 */
@DisplayName("Task 1 – Core Interfaces & DTOs")
class CoreDtoTest {

    // -------------------------------------------------------------------------
    // Inline minimal MockLLMProvider (will be replaced by the full Task 2 impl)
    // -------------------------------------------------------------------------

    /**
     * PATTERN: Strategy — demonstrates that any class implementing {@link LLMProvider} can be
     * used wherever an {@code LLMProvider} is expected, including in tests.
     */
    private static class InlineMockProvider implements LLMProvider {
        private final String fixedResponse;
        private final String modelName;

        InlineMockProvider(String fixedResponse, String modelName) {
            this.fixedResponse = fixedResponse;
            this.modelName     = modelName;
        }

        @Override
        public LLMResponse call(LLMRequest request) throws LLMException {
            return new LLMResponse(fixedResponse, modelName, 0L);
        }
    }

    // -------------------------------------------------------------------------
    // LLMRequest tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("LLMRequest")
    class LLMRequestTests {

        @Test
        @DisplayName("canonical constructor accepts valid inputs")
        void validConstructionSucceeds() {
            assertDoesNotThrow(() -> new LLMRequest("gemma2:2b", "Evaluate this code.", 0.1, 1024));
        }

        @Test
        @DisplayName("withDefaults() factory creates request with temperature=0.1 and maxTokens=2048")
        void withDefaultsFactory() {
            LLMRequest req = LLMRequest.withDefaults("gemma2:2b", "prompt");
            assertEquals(0.1,  req.temperature(), 1e-9);
            assertEquals(2048, req.maxTokens());
        }

        @Test
        @DisplayName("null model throws IllegalArgumentException")
        void nullModelThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMRequest(null, "prompt", 0.1, 100));
        }

        @Test
        @DisplayName("blank model throws IllegalArgumentException")
        void blankModelThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMRequest("   ", "prompt", 0.1, 100));
        }

        @Test
        @DisplayName("null prompt throws IllegalArgumentException")
        void nullPromptThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMRequest("gemma2:2b", null, 0.1, 100));
        }

        @Test
        @DisplayName("temperature below 0.0 throws IllegalArgumentException")
        void temperatureTooLowThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMRequest("gemma2:2b", "prompt", -0.1, 100));
        }

        @Test
        @DisplayName("temperature above 1.0 throws IllegalArgumentException")
        void temperatureTooHighThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMRequest("gemma2:2b", "prompt", 1.1, 100));
        }

        @Test
        @DisplayName("maxTokens=0 throws IllegalArgumentException")
        void zeroMaxTokensThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMRequest("gemma2:2b", "prompt", 0.1, 0));
        }

        @Test
        @DisplayName("toString() does NOT contain the prompt (security)")
        void toStringDoesNotLeakPrompt() {
            LLMRequest req = LLMRequest.withDefaults("gemma2:2b", "TOP_SECRET_PROMPT");
            assertFalse(req.toString().contains("TOP_SECRET_PROMPT"),
                    "toString() must not expose the prompt to prevent log leakage.");
        }

        @Test
        @DisplayName("toString() contains model name and promptLength")
        void toStringContainsModelAndLength() {
            LLMRequest req = LLMRequest.withDefaults("gemma2:2b", "hello");
            assertTrue(req.toString().contains("gemma2:2b"));
            assertTrue(req.toString().contains("promptLength=5"));
        }
    }

    // -------------------------------------------------------------------------
    // LLMResponse tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("LLMResponse")
    class LLMResponseTests {

        @Test
        @DisplayName("valid construction succeeds")
        void validConstructionSucceeds() {
            assertDoesNotThrow(() -> new LLMResponse("{\"key\":\"val\"}", "gemma2:2b", 123L));
        }

        @Test
        @DisplayName("null rawContent throws IllegalArgumentException")
        void nullRawContentThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMResponse(null, "gemma2:2b", 0L));
        }

        @Test
        @DisplayName("blank modelUsed throws IllegalArgumentException")
        void blankModelUsedThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMResponse("{}", "  ", 0L));
        }

        @Test
        @DisplayName("negative durationMillis throws IllegalArgumentException")
        void negativeDurationThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new LLMResponse("{}", "gemma2:2b", -1L));
        }

        @Test
        @DisplayName("looksLikeJson() returns true for JSON object string")
        void looksLikeJsonObject() {
            LLMResponse r = new LLMResponse("{\"score\":7}", "gemma2:2b", 10L);
            assertTrue(r.looksLikeJson());
        }

        @Test
        @DisplayName("looksLikeJson() returns true for JSON array string")
        void looksLikeJsonArray() {
            LLMResponse r = new LLMResponse("[1,2,3]", "gemma2:2b", 10L);
            assertTrue(r.looksLikeJson());
        }

        @Test
        @DisplayName("looksLikeJson() returns false for plain text")
        void looksLikeJsonPlainText() {
            LLMResponse r = new LLMResponse("I cannot evaluate this.", "gemma2:2b", 10L);
            assertFalse(r.looksLikeJson());
        }

        @Test
        @DisplayName("looksLikeJson() handles leading whitespace")
        void looksLikeJsonWithLeadingWhitespace() {
            LLMResponse r = new LLMResponse("   { \"k\": 1 }", "gemma2:2b", 10L);
            assertTrue(r.looksLikeJson());
        }

        @Test
        @DisplayName("empty rawContent is allowed and looksLikeJson() returns false")
        void emptyRawContentAllowed() {
            LLMResponse r = new LLMResponse("", "gemma2:2b", 0L);
            assertNotNull(r);
            assertFalse(r.looksLikeJson());
        }
    }

    // -------------------------------------------------------------------------
    // CriterionResult tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("CriterionResult")
    class CriterionResultTests {

        private static final String VALID_JSON = """
                {
                  "criterion": "SOLID Principles",
                  "score":     7,
                  "maxScore":  10,
                  "feedback":  "Good SRP adherence; DIP violated in ServiceLocator.",
                  "issues":    ["ServiceLocator couples high-level modules to implementations."]
                }
                """;

        @Test
        @DisplayName("Jackson deserializes valid JSON correctly")
        void deserializesValidJson() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            CriterionResult result = mapper.readValue(VALID_JSON, CriterionResult.class);

            assertEquals("SOLID Principles", result.criterion());
            assertEquals(7,  result.score());
            assertEquals(10, result.maxScore());
            assertFalse(result.feedback().isBlank());
            assertEquals(1, result.issues().size());
        }

        @Test
        @DisplayName("isValid() returns true for a well-formed result")
        void isValidReturnsTrueForWellFormed() throws Exception {
            CriterionResult result = new ObjectMapper().readValue(VALID_JSON, CriterionResult.class);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("isValid() returns false when score exceeds maxScore")
        void isValidReturnsFalseWhenScoreExceedsMax() {
            CriterionResult bad = new CriterionResult("Arch", 11, 10, "ok", null);
            assertFalse(bad.isValid());
        }

        @Test
        @DisplayName("isValid() returns false when maxScore is zero")
        void isValidReturnsFalseWhenMaxScoreZero() {
            CriterionResult bad = new CriterionResult("Arch", 0, 0, "ok", null);
            assertFalse(bad.isValid());
        }

        @Test
        @DisplayName("isValid() returns false when score is negative")
        void isValidReturnsFalseWhenScoreNegative() {
            CriterionResult bad = new CriterionResult("Arch", -1, 10, "ok", null);
            assertFalse(bad.isValid());
        }

        @Test
        @DisplayName("scorePercent() returns correct percentage")
        void scorePercentCorrect() {
            CriterionResult r = new CriterionResult("Arch", 7, 10, "ok", null);
            assertEquals(70.0, r.scorePercent(), 1e-9);
        }

        @Test
        @DisplayName("scorePercent() returns 0.0 when maxScore is 0 (no division by zero)")
        void scorePercentGuardsDivisionByZero() {
            CriterionResult r = new CriterionResult("Arch", 0, 0, "ok", null);
            assertEquals(0.0, r.scorePercent(), 1e-9);
        }

        @Test
        @DisplayName("null issues list is normalised to empty list")
        void nullIssuesNormalisedToEmptyList() {
            CriterionResult r = new CriterionResult("Arch", 5, 10, "ok", null);
            assertNotNull(r.issues());
            assertTrue(r.issues().isEmpty());
        }

        @Test
        @DisplayName("@JsonIgnoreProperties: unknown JSON fields do not cause deserialization failure")
        void unknownFieldsIgnored() {
            String jsonWithExtraField = """
                    {
                      "criterion": "Tests",
                      "score":     8,
                      "maxScore":  10,
                      "feedback":  "Good coverage.",
                      "issues":    [],
                      "reasoning": "chain-of-thought content that should be silently ignored"
                    }
                    """;
            assertDoesNotThrow(() -> new ObjectMapper().readValue(jsonWithExtraField, CriterionResult.class));
        }

        @Test
        @DisplayName("null criterion throws NullPointerException at construction")
        void nullCriterionThrows() {
            assertThrows(NullPointerException.class,
                    () -> new CriterionResult(null, 5, 10, "ok", null));
        }

        @Test
        @DisplayName("null feedback throws NullPointerException at construction")
        void nullFeedbackThrows() {
            assertThrows(NullPointerException.class,
                    () -> new CriterionResult("Arch", 5, 10, null, null));
        }
    }

    // -------------------------------------------------------------------------
    // LLMProvider contract tests (via InlineMockProvider)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("LLMProvider (Strategy pattern contract)")
    class LLMProviderContractTests {

        @Test
        @DisplayName("InlineMockProvider satisfies LLMProvider contract")
        void mockProviderSatisfiesContract() throws LLMException {
            // PATTERN: Strategy — the variable type is the interface, not the concrete class.
            LLMProvider provider = new InlineMockProvider("{\"key\":\"value\"}", "mock-model");
            LLMRequest  request  = LLMRequest.withDefaults("mock-model", "evaluate this");
            LLMResponse response = provider.call(request);

            assertNotNull(response);
            assertNotNull(response.rawContent());
            assertEquals("mock-model", response.modelUsed());
        }

        @Test
        @DisplayName("provider response looksLikeJson() when mock returns JSON")
        void mockResponseLooksLikeJson() throws LLMException {
            LLMProvider provider = new InlineMockProvider(
                    "{\"criterion\":\"Architecture\",\"score\":8,\"maxScore\":10,"
                    + "\"feedback\":\"OK\",\"issues\":[]}",
                    "mock-model"
            );
            LLMResponse response = provider.call(LLMRequest.withDefaults("mock-model", "prompt"));
            assertTrue(response.looksLikeJson());
        }

        @Test
        @DisplayName("LLMException is a checked exception (extends Exception)")
        void llmExceptionIsChecked() {
            LLMException ex = new LLMException("test error");
            assertInstanceOf(Exception.class, ex);
            assertEquals("test error", ex.getMessage());
        }

        @Test
        @DisplayName("LLMException wraps root cause correctly")
        void llmExceptionWrapsRootCause() {
            RuntimeException root = new RuntimeException("network failure");
            LLMException ex = new LLMException("Provider call failed", root);
            assertSame(root, ex.getCause());
        }
    }
}
