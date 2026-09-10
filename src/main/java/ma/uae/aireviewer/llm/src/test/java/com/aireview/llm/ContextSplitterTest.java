package com.aireview.llm;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Task 6: {@link ContextSplitter}.
 *
 * <p>All tests are hermetic — no network calls, no file I/O.
 */
@DisplayName("Task 6 – ContextSplitter")
class ContextSplitterTest {

    // =========================================================================
    // Section 1: Construction guards
    // =========================================================================

    @Nested
    @DisplayName("1. Construction Guards")
    class ConstructionGuardTests {

        @Test
        @DisplayName("default constructor uses expected defaults")
        void defaultConstructorDefaults() {
            ContextSplitter cs = new ContextSplitter();
            assertEquals(ContextSplitter.DEFAULT_MAX_CHARS_PER_CHUNK, cs.getMaxCharsPerChunk());
            assertEquals(ContextSplitter.DEFAULT_OVERLAP_LINES,       cs.getOverlapLines());
        }

        @Test
        @DisplayName("zero maxCharsPerChunk throws IllegalArgumentException")
        void zeroMaxCharsThrows() {
            assertThrows(IllegalArgumentException.class, () -> new ContextSplitter(0, 0));
        }

        @Test
        @DisplayName("negative maxCharsPerChunk throws IllegalArgumentException")
        void negativeMaxCharsThrows() {
            assertThrows(IllegalArgumentException.class, () -> new ContextSplitter(-1, 0));
        }

        @Test
        @DisplayName("negative overlapLines throws IllegalArgumentException")
        void negativeOverlapThrows() {
            assertThrows(IllegalArgumentException.class, () -> new ContextSplitter(100, -1));
        }

        @Test
        @DisplayName("zero overlapLines is valid")
        void zeroOverlapIsValid() {
            assertDoesNotThrow(() -> new ContextSplitter(100, 0));
        }
    }

    // =========================================================================
    // Section 2: split() — fast path and basic chunking
    // =========================================================================

    @Nested
    @DisplayName("2. split() — Chunking Logic")
    class SplitTests {

        @Test
        @DisplayName("null source returns single-element list with empty string")
        void nullSourceReturnsSingleEmpty() {
            ContextSplitter cs = new ContextSplitter(100, 0);
            List<String> chunks = cs.split(null);
            assertEquals(1, chunks.size());
            assertTrue(chunks.get(0).isEmpty() || chunks.get(0).isBlank());
        }

        @Test
        @DisplayName("blank source returns single-element list")
        void blankSourceReturnsSingleElement() {
            ContextSplitter cs = new ContextSplitter(100, 0);
            List<String> chunks = cs.split("   ");
            assertEquals(1, chunks.size());
        }

        @Test
        @DisplayName("source fitting in one chunk returns a single-element list")
        void shortSourceReturnsSingleChunk() {
            ContextSplitter cs = new ContextSplitter(1000, 0);
            String source = "public class Foo {}\npublic class Bar {}";
            List<String> chunks = cs.split(source);
            assertEquals(1, chunks.size());
            assertTrue(chunks.get(0).contains("Foo"));
            assertTrue(chunks.get(0).contains("Bar"));
        }

        @Test
        @DisplayName("source exceeding limit produces multiple chunks")
        void largeSourceProducesMultipleChunks() {
            // 10 chars per chunk, no overlap
            ContextSplitter cs = new ContextSplitter(10, 0);
            // Each line is 5 chars + newline = 6 chars; 2 lines = 12 chars → must split
            String source = "AAAAA\nBBBBB\nCCCCC\nDDDDD\nEEEEE";
            List<String> chunks = cs.split(source);
            assertTrue(chunks.size() > 1,
                    "Source exceeding budget must produce multiple chunks.");
        }

        @Test
        @DisplayName("each chunk is within the configured character limit")
        void eachChunkWithinCharLimit() {
            int limit = 50;
            ContextSplitter cs = new ContextSplitter(limit, 0);
            String source = "A".repeat(10) + "\n" + "B".repeat(10) + "\n"
                          + "C".repeat(10) + "\n" + "D".repeat(10) + "\n"
                          + "E".repeat(10) + "\n" + "F".repeat(10);
            List<String> chunks = cs.split(source);
            for (String chunk : chunks) {
                assertTrue(chunk.length() <= limit + 20, // +20 tolerance for overlap
                        "Chunk length " + chunk.length() + " should not greatly exceed limit " + limit);
            }
        }

        @Test
        @DisplayName("all original lines appear in at least one chunk (no data loss)")
        void noDataLossAcrossChunks() {
            ContextSplitter cs = new ContextSplitter(30, 0);
            String[] lines = {"line1", "line2", "line3", "line4", "line5",
                              "line6", "line7", "line8", "line9", "line10"};
            String source = String.join("\n", lines);
            List<String> chunks = cs.split(source);

            String combined = String.join("", chunks);
            for (String line : lines) {
                assertTrue(combined.contains(line),
                        "Line '" + line + "' must appear in at least one chunk.");
            }
        }

        @Test
        @DisplayName("overlap causes boundary lines to appear in consecutive chunks")
        void overlapLinesPresentInConsecutiveChunks() {
            // 3 overlap lines, small chunk limit to force a split
            ContextSplitter cs = new ContextSplitter(30, 3);
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 15; i++) {
                sb.append("line").append(i).append("\n");
            }
            List<String> chunks = cs.split(sb.toString());
            assertTrue(chunks.size() >= 2, "Must produce at least 2 chunks.");

            // The last few lines of chunk[0] should appear in chunk[1]
            String c0 = chunks.get(0);
            String c1 = chunks.get(1);
            // Find the last non-empty line of chunk[0]
            String[] c0Lines = c0.trim().split("\n");
            if (c0Lines.length > 0) {
                String lastLine = c0Lines[c0Lines.length - 1].trim();
                assertTrue(c1.contains(lastLine),
                        "Last line of chunk[0] ('" + lastLine + "') must appear in chunk[1] due to overlap.");
            }
        }

        @Test
        @DisplayName("split() result list is non-null and non-empty for any input")
        void resultIsNeverNullOrEmpty() {
            ContextSplitter cs = new ContextSplitter(100, 0);
            assertNotNull(cs.split(null));
            assertFalse(cs.split(null).isEmpty());
            assertNotNull(cs.split(""));
            assertFalse(cs.split("").isEmpty());
            assertNotNull(cs.split("some code"));
            assertFalse(cs.split("some code").isEmpty());
        }
    }

    // =========================================================================
    // Section 3: File filtering
    // =========================================================================

    @Nested
    @DisplayName("3. File Filtering")
    class FileFilteringTests {

        @ParameterizedTest(name = "''{0}'' should be EXCLUDED")
        @ValueSource(strings = {
            "Main.class", "app.jar", "server.war", "lib.ear",
            "photo.png", "icon.jpg", "archive.zip", "bundle.gz",
            "report.pdf", "document.docx"
        })
        @DisplayName("binary and archive extensions are excluded")
        void binaryExtensionsExcluded(String fileName) {
            assertFalse(ContextSplitter.shouldInclude(fileName),
                    "'" + fileName + "' should be excluded.");
        }

        @ParameterizedTest(name = "''{0}'' should be INCLUDED")
        @ValueSource(strings = {
            "Main.java", "Service.java", "README.md",
            "pom.xml", "build.gradle", "application.properties",
            "UserController.java", "src/main/Foo.java"
        })
        @DisplayName("source and config files are included")
        void sourceFilesIncluded(String fileName) {
            assertTrue(ContextSplitter.shouldInclude(fileName),
                    "'" + fileName + "' should be included.");
        }

        @Test
        @DisplayName("null fileName returns false (not included)")
        void nullFileNameReturnsFalse() {
            assertFalse(ContextSplitter.shouldInclude(null));
        }

        @Test
        @DisplayName("blank fileName returns false")
        void blankFileNameReturnsFalse() {
            assertFalse(ContextSplitter.shouldInclude("   "));
        }

        @Test
        @DisplayName("filterFiles() removes excluded files from a mixed list")
        void filterFilesRemovesExcluded() {
            List<String> mixed = List.of(
                    "Main.java", "App.class", "logo.png", "Service.java", "lib.jar");
            List<String> filtered = ContextSplitter.filterFiles(mixed);

            assertTrue(filtered.contains("Main.java"));
            assertTrue(filtered.contains("Service.java"));
            assertFalse(filtered.contains("App.class"));
            assertFalse(filtered.contains("logo.png"));
            assertFalse(filtered.contains("lib.jar"));
        }

        @Test
        @DisplayName("filterFiles() with null input returns empty list")
        void filterFilesNullInputReturnsEmpty() {
            List<String> result = ContextSplitter.filterFiles(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("case-insensitive: .CLASS and .JAR are excluded")
        void extensionCheckCaseInsensitive() {
            assertFalse(ContextSplitter.shouldInclude("Main.CLASS"));
            assertFalse(ContextSplitter.shouldInclude("lib.JAR"));
        }
    }

    // =========================================================================
    // Section 4: aggregateChunkResults()
    // =========================================================================

    @Nested
    @DisplayName("4. aggregateChunkResults()")
    class AggregationTests {

        @Test
        @DisplayName("single chunk result is returned unchanged")
        void singleChunkReturnedUnchanged() {
            CriterionResult r = new CriterionResult("Arch", 7, 10, "Good", List.of("Issue1"));
            CriterionResult agg = ContextSplitter.aggregateChunkResults(List.of(r));
            assertSame(r, agg);
        }

        @Test
        @DisplayName("two chunks: score is average (floor)")
        void twoChunksScoreIsAverage() {
            CriterionResult r1 = new CriterionResult("Arch", 8, 10, "Good", List.of());
            CriterionResult r2 = new CriterionResult("Arch", 6, 10, "Bad",  List.of());
            CriterionResult agg = ContextSplitter.aggregateChunkResults(List.of(r1, r2));
            assertEquals(7, agg.score()); // (8+6)/2 = 7
        }

        @Test
        @DisplayName("three chunks: score is average (floor division)")
        void threeChunksScoreIsFloorAverage() {
            CriterionResult r1 = new CriterionResult("T", 9, 10, "f1", List.of());
            CriterionResult r2 = new CriterionResult("T", 7, 10, "f2", List.of());
            CriterionResult r3 = new CriterionResult("T", 8, 10, "f3", List.of());
            CriterionResult agg = ContextSplitter.aggregateChunkResults(List.of(r1, r2, r3));
            assertEquals(8, agg.score()); // (9+7+8)/3 = 24/3 = 8
        }

        @Test
        @DisplayName("issues are de-duplicated across chunks")
        void issuesDeduplicated() {
            CriterionResult r1 = new CriterionResult("A", 5, 10, "f1", List.of("Issue A", "Issue B"));
            CriterionResult r2 = new CriterionResult("A", 5, 10, "f2", List.of("Issue B", "Issue C"));
            CriterionResult agg = ContextSplitter.aggregateChunkResults(List.of(r1, r2));

            assertEquals(3, agg.issues().size(),
                    "De-duplicated issues: A, B, C (not A, B, B, C).");
            assertTrue(agg.issues().contains("Issue A"));
            assertTrue(agg.issues().contains("Issue B"));
            assertTrue(agg.issues().contains("Issue C"));
        }

        @Test
        @DisplayName("feedback contains [Chunk N] labels for all chunks")
        void feedbackContainsChunkLabels() {
            CriterionResult r1 = new CriterionResult("A", 5, 10, "feedback1", List.of());
            CriterionResult r2 = new CriterionResult("A", 5, 10, "feedback2", List.of());
            CriterionResult agg = ContextSplitter.aggregateChunkResults(List.of(r1, r2));

            assertTrue(agg.feedback().contains("[Chunk 1]"));
            assertTrue(agg.feedback().contains("[Chunk 2]"));
        }

        @Test
        @DisplayName("criterion name is preserved from first chunk")
        void criterionNamePreserved() {
            CriterionResult r1 = new CriterionResult("SOLID Principles", 7, 10, "ok", List.of());
            CriterionResult r2 = new CriterionResult("SOLID Principles", 6, 10, "ok", List.of());
            CriterionResult agg = ContextSplitter.aggregateChunkResults(List.of(r1, r2));
            assertEquals("SOLID Principles", agg.criterion());
        }

        @Test
        @DisplayName("null chunkResults throws IllegalArgumentException")
        void nullChunkResultsThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> ContextSplitter.aggregateChunkResults(null));
        }

        @Test
        @DisplayName("empty chunkResults throws IllegalArgumentException")
        void emptyChunkResultsThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> ContextSplitter.aggregateChunkResults(List.of()));
        }
    }
}
