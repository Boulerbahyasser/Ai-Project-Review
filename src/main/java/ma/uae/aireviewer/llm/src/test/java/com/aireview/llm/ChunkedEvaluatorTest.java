package com.aireview.llm;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Task 6: {@link ChunkedEvaluator}.
 *
 * <p>All tests are hermetic — {@link MockLLMProvider} is used throughout.
 * The {@link ContextSplitter} is configured with small chunk sizes to force
 * multi-chunk paths without generating large synthetic source strings.
 */
@DisplayName("Task 6 – ChunkedEvaluator")
class ChunkedEvaluatorTest {

    private static final String MODEL   = "gemma2:2b";
    private static final String PROJECT = "TestProject";

    /** Short source that fits in any reasonable chunk. */
    private static final String SHORT_SOURCE =
            "public class Foo { private Service svc; }";

    // =========================================================================
    // Section 1: Construction guards
    // =========================================================================

    @Nested
    @DisplayName("1. Construction Guards")
    class ConstructionGuardTests {

        @Test
        @DisplayName("null resilientEvaluator throws IllegalArgumentException")
        void nullEvaluatorThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ChunkedEvaluator(null, new ContextSplitter()));
        }

        @Test
        @DisplayName("null splitter throws IllegalArgumentException")
        void nullSplitterThrows() {
            ResilientEvaluator ev = new ResilientEvaluator(new MockLLMProvider(), 1, 0L, false);
            assertThrows(IllegalArgumentException.class,
                    () -> new ChunkedEvaluator(ev, null));
        }

        @Test
        @DisplayName("convenience constructor (LLMProvider) creates evaluator successfully")
        void convenienceConstructorSucceeds() {
            assertDoesNotThrow(() -> new ChunkedEvaluator(new MockLLMProvider()));
        }

        @Test
        @DisplayName("getSplitter() returns the configured splitter")
        void getSplitterReturnsConfigured() {
            ContextSplitter    splitter = new ContextSplitter(500, 2);
            ResilientEvaluator ev       = new ResilientEvaluator(new MockLLMProvider(), 1, 0L, false);
            ChunkedEvaluator   chunked  = new ChunkedEvaluator(ev, splitter);
            assertSame(splitter, chunked.getSplitter());
        }

        @Test
        @DisplayName("evaluate() with null projectName throws IllegalArgumentException")
        void nullProjectNameThrows() {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 9999);
            assertThrows(IllegalArgumentException.class,
                    () -> chunked.evaluate(null, SHORT_SOURCE, MODEL));
        }

        @Test
        @DisplayName("evaluate() with null model throws IllegalArgumentException")
        void nullModelThrows() {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 9999);
            assertThrows(IllegalArgumentException.class,
                    () -> chunked.evaluate(PROJECT, SHORT_SOURCE, null));
        }
    }

    // =========================================================================
    // Section 2: Fast-path (single chunk)
    // =========================================================================

    @Nested
    @DisplayName("2. Fast-Path — Single Chunk (source fits in one chunk)")
    class FastPathTests {

        @Test
        @DisplayName("short source returns a valid EvaluationResult")
        void shortSourceReturnsValidResult() throws LLMException {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 9999);
            EvaluationResult result  = chunked.evaluate(PROJECT, SHORT_SOURCE, MODEL);
            assertNotNull(result);
        }

        @Test
        @DisplayName("short source: result has all criteria")
        void shortSourceHasAllCriteria() throws LLMException {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 9999);
            EvaluationResult result  = chunked.evaluate(PROJECT, SHORT_SOURCE, MODEL);
            assertEquals(EvaluationCriterion.values().length, result.results().size());
        }

        @Test
        @DisplayName("short source: all criterion results are valid")
        void shortSourceAllValid() throws LLMException {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 9999);
            EvaluationResult result  = chunked.evaluate(PROJECT, SHORT_SOURCE, MODEL);
            for (CriterionResult cr : result.results()) {
                assertTrue(cr.isValid(), "CriterionResult for '" + cr.criterion() + "' must be valid.");
            }
        }

        @Test
        @DisplayName("fast-path: mock called exactly once per criterion")
        void fastPathCallsOncePerCriterion() throws LLMException {
            MockLLMProvider mock    = new MockLLMProvider();
            ChunkedEvaluator chunked = makeChunked(mock, 9999);
            chunked.evaluate(PROJECT, SHORT_SOURCE, MODEL);
            assertEquals(EvaluationCriterion.values().length, mock.getCallCount());
        }
    }

    // =========================================================================
    // Section 3: Multi-chunk path
    // =========================================================================

    @Nested
    @DisplayName("3. Multi-Chunk Path (source exceeds chunk limit)")
    class MultiChunkTests {

        /**
         * Builds a source string guaranteed to produce N chunks at the given limit.
         * Each line is exactly {@code lineLen} characters long.
         */
        private static String buildSource(int totalLines, int lineLen) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < totalLines; i++) {
                sb.append("A".repeat(lineLen)).append("\n");
            }
            return sb.toString();
        }

        @Test
        @DisplayName("large source is split and all criteria are evaluated")
        void largeSourceEvaluatedAcrossChunks() throws LLMException {
            // 20-char limit forces splitting of a 200-char source
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 20);
            String source = buildSource(20, 9); // 20 lines × 10 chars = 200 chars total
            EvaluationResult result = chunked.evaluate(PROJECT, source, MODEL);

            assertNotNull(result);
            assertEquals(EvaluationCriterion.values().length, result.results().size());
        }

        @Test
        @DisplayName("all criterion results pass isValid() after aggregation")
        void aggregatedResultsValid() throws LLMException {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 20);
            String source = buildSource(20, 9);
            EvaluationResult result = chunked.evaluate(PROJECT, source, MODEL);

            for (CriterionResult cr : result.results()) {
                assertTrue(cr.isValid(),
                        "Aggregated result for '" + cr.criterion() + "' must pass isValid().");
            }
        }

        @Test
        @DisplayName("multi-chunk: mock called at least once per criterion per chunk")
        void multiChunkCallCountAtLeastCriteriaTimesChunks() throws LLMException {
            MockLLMProvider mock    = new MockLLMProvider();
            int             limit   = 20; // small limit → multiple chunks
            ChunkedEvaluator chunked = makeChunked(mock, limit);
            String source = buildSource(20, 9); // will produce >1 chunk

            int numChunks = new ContextSplitter(limit, 0).split(source).size();
            chunked.evaluate(PROJECT, source, MODEL);

            int expectedMinCalls = EvaluationCriterion.values().length * numChunks;
            assertTrue(mock.getCallCount() >= expectedMinCalls,
                    "With " + numChunks + " chunks and " + EvaluationCriterion.values().length
                    + " criteria, expected at least " + expectedMinCalls + " calls, got "
                    + mock.getCallCount());
        }

        @Test
        @DisplayName("projectName is preserved in aggregated EvaluationResult")
        void projectNamePreservedAfterChunking() throws LLMException {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 20);
            String source = buildSource(10, 9);
            EvaluationResult result = chunked.evaluate("ChunkedProject", source, MODEL);
            assertEquals("ChunkedProject", result.projectName());
        }

        @Test
        @DisplayName("partial criteria evaluation works with chunking")
        void partialCriteriaWithChunking() throws LLMException {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 20);
            String source = buildSource(10, 9);
            EvaluationResult result = chunked.evaluate(
                    PROJECT, source, MODEL,
                    List.of(EvaluationCriterion.ARCHITECTURE));

            // Verify exactly one criterion result was returned (the one we requested)
            assertEquals(1, result.results().size(),
                    "Partial evaluation must produce exactly one result.");
            assertFalse(result.results().get(0).criterion().isBlank(),
                    "Criterion name must not be blank.");
        }
    }

    // =========================================================================
    // Section 4: Error propagation
    // =========================================================================

    @Nested
    @DisplayName("4. Error Propagation")
    class ErrorPropagationTests {

        @Test
        @DisplayName("always-failing provider propagates LLMException through ChunkedEvaluator")
        void failingProviderPropagatesToCaller() {
            LLMProvider alwaysFail = request -> { throw new LLMException("always fails"); };
            ResilientEvaluator ev     = new ResilientEvaluator(alwaysFail, 1, 0L, false);
            ChunkedEvaluator   chunked = new ChunkedEvaluator(ev, new ContextSplitter());

            assertThrows(LLMException.class,
                    () -> chunked.evaluate(PROJECT, SHORT_SOURCE, MODEL));
        }

        @Test
        @DisplayName("empty criteria list throws IllegalArgumentException")
        void emptyCriteriaThrows() {
            ChunkedEvaluator chunked = makeChunked(new MockLLMProvider(), 9999);
            assertThrows(IllegalArgumentException.class,
                    () -> chunked.evaluate(PROJECT, SHORT_SOURCE, MODEL, List.of()));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    /**
     * Creates a {@link ChunkedEvaluator} backed by a no-sleep {@link ResilientEvaluator}
     * and a {@link ContextSplitter} with the given character limit and no overlap.
     */
    private static ChunkedEvaluator makeChunked(LLMProvider provider, int chunkLimit) {
        ResilientEvaluator ev      = new ResilientEvaluator(provider, 3, 0L, false);
        ContextSplitter    splitter = new ContextSplitter(chunkLimit, 0);
        return new ChunkedEvaluator(ev, splitter);
    }
}
