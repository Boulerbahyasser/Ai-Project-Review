package com.aireview.llm;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Task 8: LaTeX Report Generator.
 *
 * <p><b>Testing strategy:</b> All tests are hermetic — no network calls, no Ollama process.
 * {@link LatexReportBuilder} is exercised using {@link MockLLMProvider}-derived fixture data
 * ({@link EvaluationResult} and {@link CriterionResult} instances built directly in-process),
 * consistent with the project rule that every task's tests utilise {@code MockLLMProvider}.
 *
 * <p>The suite is organised into four sections:
 * <ol>
 *   <li>LaTeX escape correctness — every special character is handled.</li>
 *   <li>Builder API — fluent setter coverage, defaults, null safety.</li>
 *   <li>Document structure — generated output contains required sections/strings.</li>
 *   <li>File I/O — {@code buildAndWrite()} creates a valid file in a temp directory.</li>
 * </ol>
 */
@DisplayName("Task 8 – LaTeX Report Generator (LatexReportBuilder)")
class LatexReportBuilderTest {

    // =========================================================================
    // Shared fixtures
    // =========================================================================

    private static final String MODEL = "gemma2:2b";

    /**
     * Builds a minimal but valid {@link EvaluationResult} via {@link MockLLMProvider}.
     */
    static EvaluationResult minimalResult() throws LLMException {
        MockLLMProvider mock = new MockLLMProvider();
        ResilientEvaluator ev = new ResilientEvaluator(mock, 1, 0L);
        return ev.evaluate("TestProject", "public class Foo {}", MODEL,
                List.of(EvaluationCriterion.ARCHITECTURE));
    }

    /**
     * Builds a full multi-criterion result (all three criteria) via {@link MockLLMProvider}.
     */
    static EvaluationResult fullResult() throws LLMException {
        MockLLMProvider mock = new MockLLMProvider();
        ResilientEvaluator ev = new ResilientEvaluator(mock, 1, 0L);
        return ev.evaluate("FullProject", "public class Bar {}", MODEL);
    }

    // =========================================================================
    // Section 1: LaTeX Escaping
    // =========================================================================

    @Nested
    @DisplayName("1. LaTeX Character Escaping (escape())")
    class EscapingTests {

        @Test
        @DisplayName("null input returns empty string")
        void nullReturnsEmpty() {
            assertEquals("", LatexReportBuilder.escape(null));
        }

        @Test
        @DisplayName("plain text is returned unchanged")
        void plainTextUnchanged() {
            assertEquals("Hello World", LatexReportBuilder.escape("Hello World"));
        }

        @Test
        @DisplayName("backslash is escaped (no double-escaping via sentinel)")
        void backslashEscaped() {
            String escaped = LatexReportBuilder.escape("a\\b");
            assertTrue(escaped.contains("\\textbackslash{}"),
                    "Backslash must be escaped to \\textbackslash{}");
            assertFalse(escaped.contains("\\\\"),
                    "Must not produce double-backslash");
        }

        @Test
        @DisplayName("percent sign is escaped")
        void percentEscaped() {
            assertEquals("50\\%", LatexReportBuilder.escape("50%"));
        }

        @Test
        @DisplayName("ampersand is escaped")
        void ampersandEscaped() {
            assertEquals("A \\& B", LatexReportBuilder.escape("A & B"));
        }

        @Test
        @DisplayName("underscore is escaped")
        void underscoreEscaped() {
            assertEquals("some\\_variable", LatexReportBuilder.escape("some_variable"));
        }

        @Test
        @DisplayName("dollar sign is escaped")
        void dollarEscaped() {
            assertEquals("\\$100", LatexReportBuilder.escape("$100"));
        }

        @Test
        @DisplayName("hash is escaped")
        void hashEscaped() {
            assertEquals("\\#1", LatexReportBuilder.escape("#1"));
        }

        @Test
        @DisplayName("curly braces are escaped")
        void curlyBracesEscaped() {
            assertEquals("\\{value\\}", LatexReportBuilder.escape("{value}"));
        }

        @Test
        @DisplayName("caret is escaped")
        void caretEscaped() {
            assertTrue(LatexReportBuilder.escape("x^2").contains("\\textasciicircum{}"));
        }

        @Test
        @DisplayName("tilde is escaped")
        void tildeEscaped() {
            assertTrue(LatexReportBuilder.escape("~home").contains("\\textasciitilde{}"));
        }

        @Test
        @DisplayName("angle brackets are escaped")
        void angleBracketsEscaped() {
            String escaped = LatexReportBuilder.escape("<untrusted_code>");
            assertTrue(escaped.contains("\\textless{}"),   "< must be escaped");
            assertTrue(escaped.contains("\\textgreater{}"), "> must be escaped");
        }

        @Test
        @DisplayName("all special chars combined are all escaped")
        void allSpecialCharsEscaped() {
            String escaped = LatexReportBuilder.escape("& % $ # _ { } ^ ~ \\ < >");
            assertFalse(escaped.contains(" & "),  "bare & must be gone");
            assertFalse(escaped.contains(" % "),  "bare % must be gone");
            assertFalse(escaped.contains(" $ "),  "bare $ must be gone");
            assertFalse(escaped.contains(" # "),  "bare # must be gone");
        }
    }

    // =========================================================================
    // Section 2: Builder API
    // =========================================================================

    @Nested
    @DisplayName("2. Builder API")
    class BuilderApiTests {

        @Test
        @DisplayName("forResult() rejects null EvaluationResult")
        void nullResultThrows() {
            assertThrows(NullPointerException.class,
                    () -> LatexReportBuilder.forResult(null));
        }

        @Test
        @DisplayName("withOutputPath() rejects null path")
        void nullPathThrows() throws LLMException {
            assertThrows(NullPointerException.class,
                    () -> LatexReportBuilder.forResult(minimalResult()).withOutputPath(null));
        }

        @Test
        @DisplayName("withModel(null) silently defaults to 'unknown'")
        void nullModelDefaultsToUnknown() throws LLMException {
            String doc = LatexReportBuilder.forResult(minimalResult()).withModel(null).build();
            assertTrue(doc.contains("unknown"));
        }

        @Test
        @DisplayName("withModel(blank) silently defaults to 'unknown'")
        void blankModelDefaultsToUnknown() throws LLMException {
            String doc = LatexReportBuilder.forResult(minimalResult()).withModel("   ").build();
            assertTrue(doc.contains("unknown"));
        }

        @Test
        @DisplayName("withAnalysisDate(null) is silently ignored")
        void nullDateIgnored() throws LLMException {
            assertDoesNotThrow(() -> LatexReportBuilder.forResult(minimalResult())
                    .withAnalysisDate(null).build());
        }

        @Test
        @DisplayName("withAnalysisDate sets a custom date string")
        void customDateApplied() throws LLMException {
            String doc = LatexReportBuilder.forResult(minimalResult())
                    .withAnalysisDate("January 1, 2025").build();
            assertTrue(doc.contains("January 1, 2025"));
        }

        @Test
        @DisplayName("PATTERN: Builder — full fluent chaining compiles without error")
        void fluentChainingCompiles() throws LLMException {
            String doc = LatexReportBuilder.forResult(minimalResult())
                    .withModel(MODEL)
                    .withAnalysisDate("September 11, 2026")
                    .withOutputPath(Path.of("dummy.tex"))
                    .build();
            assertNotNull(doc);
            assertFalse(doc.isBlank());
        }
    }

    // =========================================================================
    // Section 3: Document Structure
    // =========================================================================

    @Nested
    @DisplayName("3. Document Structure")
    class DocumentStructureTests {

        @Test
        @DisplayName("generated document begins with \\documentclass")
        void beginsWithDocumentClass() throws LLMException {
            assertTrue(LatexReportBuilder.forResult(minimalResult()).build()
                    .contains("\\documentclass"));
        }

        @Test
        @DisplayName("generated document ends with \\end{document}")
        void endsWithEndDocument() throws LLMException {
            assertTrue(LatexReportBuilder.forResult(minimalResult()).build()
                    .strip().endsWith("\\end{document}"));
        }

        @Test
        @DisplayName("project name appears in the document")
        void projectNamePresent() throws LLMException {
            assertTrue(LatexReportBuilder.forResult(minimalResult()).build()
                    .contains("TestProject"));
        }

        @Test
        @DisplayName("model name appears in the document")
        void modelNamePresent() throws LLMException {
            assertTrue(LatexReportBuilder.forResult(minimalResult()).withModel("gemma2:2b").build()
                    .contains("gemma2:2b"));
        }

        @Test
        @DisplayName("analysis date appears in the document")
        void analysisDatePresent() throws LLMException {
            assertTrue(LatexReportBuilder.forResult(minimalResult())
                    .withAnalysisDate("September 11, 2026").build()
                    .contains("September 11, 2026"));
        }

        @Test
        @DisplayName("summary table header row (Criterion | Score | Maximum) is present")
        void summaryTableHeaderPresent() throws LLMException {
            String doc = LatexReportBuilder.forResult(minimalResult()).build();
            assertTrue(doc.contains("Criterion"));
            assertTrue(doc.contains("Score"));
            assertTrue(doc.contains("Maximum"));
        }

        @Test
        @DisplayName("Architecture criterion appears in the summary table")
        void architectureCriterionInTable() throws LLMException {
            assertTrue(LatexReportBuilder.forResult(minimalResult()).build()
                    .contains("Architecture"));
        }

        @Test
        @DisplayName("all three criteria appear for full result")
        void allCriteriaInFullResult() throws LLMException {
            String doc = LatexReportBuilder.forResult(fullResult()).build();
            for (EvaluationCriterion c : EvaluationCriterion.values()) {
                assertTrue(doc.contains(c.getDisplayName()),
                        "Criterion '" + c.getDisplayName() + "' must appear.");
            }
        }

        @Test
        @DisplayName("metadata section contains project name and model")
        void metadataSectionPresent() throws LLMException {
            String doc = LatexReportBuilder.forResult(minimalResult()).withModel("gemma2:2b").build();
            assertTrue(doc.contains("Evaluation Metadata"));
            assertTrue(doc.contains("Model Used"));
        }

        @Test
        @DisplayName("total score row is in the summary table")
        void totalRowPresent() throws LLMException {
            assertTrue(LatexReportBuilder.forResult(minimalResult()).build().contains("Total"));
        }

        @Test
        @DisplayName("special chars in LLM feedback are escaped in output")
        void feedbackSpecialCharsEscaped() {
            CriterionResult cr = new CriterionResult(
                    "Architecture", 8, 10,
                    "Score is 80% & good — see: user_service.java",
                    List.of("Fix $Config.java to avoid & coupling"));
            EvaluationResult r = new EvaluationResult(
                    "SpecialProject", List.of(cr), cr.score(), cr.maxScore(), 500L);

            String doc = LatexReportBuilder.forResult(r).build();

            assertFalse(doc.contains(" & good"), "Bare & must be escaped.");
            assertTrue(doc.contains("\\_"), "Underscore in 'user_service' must be escaped.");
        }
    }

    // =========================================================================
    // Section 4: File I/O
    // =========================================================================

    @Nested
    @DisplayName("4. File I/O (buildAndWrite)")
    class FileIOTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("buildAndWrite() creates the file at the specified path")
        void fileIsCreated() throws LLMException, IOException {
            Path target = tempDir.resolve("evaluation.tex");
            LatexReportBuilder.forResult(minimalResult()).withOutputPath(target).buildAndWrite();
            assertTrue(Files.exists(target));
        }

        @Test
        @DisplayName("buildAndWrite() returns the output path")
        void returnsOutputPath() throws LLMException, IOException {
            Path target = tempDir.resolve("out.tex");
            Path returned = LatexReportBuilder.forResult(minimalResult())
                    .withOutputPath(target).buildAndWrite();
            assertEquals(target, returned);
        }

        @Test
        @DisplayName("written file is not empty")
        void writtenFileNotEmpty() throws LLMException, IOException {
            Path target = tempDir.resolve("test.tex");
            LatexReportBuilder.forResult(minimalResult()).withOutputPath(target).buildAndWrite();
            assertTrue(Files.size(target) > 0);
        }

        @Test
        @DisplayName("written file content matches build() output")
        void writtenContentMatchesBuild() throws LLMException, IOException {
            EvaluationResult r = minimalResult();
            Path target = tempDir.resolve("match.tex");
            String expected = LatexReportBuilder.forResult(r).withModel(MODEL).build();
            LatexReportBuilder.forResult(r).withModel(MODEL).withOutputPath(target).buildAndWrite();
            assertEquals(expected, Files.readString(target));
        }

        @Test
        @DisplayName("buildAndWrite() can overwrite on second call")
        void overwriteOnSecondCall() throws LLMException, IOException {
            Path target = tempDir.resolve("rewrite.tex");
            LatexReportBuilder.forResult(minimalResult()).withOutputPath(target).buildAndWrite();
            LatexReportBuilder.forResult(minimalResult())
                    .withAnalysisDate("December 31, 2099")
                    .withOutputPath(target).buildAndWrite();
            assertTrue(Files.exists(target));
            assertTrue(Files.size(target) > 0);
        }
    }
}
