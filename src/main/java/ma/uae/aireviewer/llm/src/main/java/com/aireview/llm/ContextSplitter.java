package com.aireview.llm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Splits large source-code strings into token-window-safe chunks for LLM evaluation.
 *
 * <p>Language models have a finite context window (typically 2048–8192 tokens). When a
 * project's concatenated source code exceeds this window, sending it as a single prompt
 * causes silent truncation — the model never sees the tail of the file, producing
 * systematically biased scores. {@code ContextSplitter} addresses this by dividing the
 * input into overlapping chunks, each small enough to fit within the configured character
 * budget, which is used as a conservative proxy for the token budget.
 *
 * <p><b>Three-stage pipeline:</b>
 * <ol>
 *   <li><b>File Filtering:</b> Non-evaluable file types (binaries, build artefacts) are
 *       stripped from the concatenated input before chunking.</li>
 *   <li><b>Chunking:</b> The filtered source is split on line boundaries into chunks of at
 *       most {@code maxCharsPerChunk} characters. Splitting on lines (rather than arbitrary
 *       character positions) ensures no token is split mid-line, preserving syntactic
 *       coherence for the model.</li>
 *   <li><b>Overlap:</b> Adjacent chunks share {@code overlapLines} lines, so that logical
 *       constructs spanning a chunk boundary (e.g., a class declaration and its first method)
 *       appear complete in at least one chunk.</li>
 * </ol>
 *
 * <p><b>Design note:</b> This class is intentionally stateless — all methods are pure
 * functions of their inputs. State (retry budgets, provider config) belongs in
 * {@link ChunkedEvaluator}.
 */
public class ContextSplitter {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    /**
     * Default maximum characters per chunk.
     * At ~4 chars/token, 6000 chars ≈ 1500 tokens — safely within a 2048-token window
     * after the system prompt and few-shot example are added.
     */
    public static final int DEFAULT_MAX_CHARS_PER_CHUNK = 6_000;

    /**
     * Default number of overlap lines between adjacent chunks.
     * Provides continuity across chunk boundaries without wasting budget.
     */
    public static final int DEFAULT_OVERLAP_LINES = 10;

    /**
     * File extensions that are excluded from evaluation.
     * These are binary or build-artefact formats that contain no evaluable Java source.
     */
    private static final Set<String> EXCLUDED_EXTENSIONS = Set.of(
            ".class", ".jar", ".war", ".ear",  // Java bytecode / archives
            ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico", // images
            ".zip", ".gz", ".tar",             // archives
            ".pdf", ".doc", ".docx",           // documents
            ".DS_Store", ".gitignore"          // metadata
    );

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final int maxCharsPerChunk;
    private final int overlapLines;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Creates a {@code ContextSplitter} with default parameters.
     */
    public ContextSplitter() {
        this(DEFAULT_MAX_CHARS_PER_CHUNK, DEFAULT_OVERLAP_LINES);
    }

    /**
     * Creates a {@code ContextSplitter} with custom parameters.
     *
     * @param maxCharsPerChunk maximum characters per chunk; must be positive
     * @param overlapLines     number of lines shared between adjacent chunks; must be non-negative
     */
    public ContextSplitter(int maxCharsPerChunk, int overlapLines) {
        if (maxCharsPerChunk <= 0) {
            throw new IllegalArgumentException(
                    "ContextSplitter: maxCharsPerChunk must be positive, got: " + maxCharsPerChunk);
        }
        if (overlapLines < 0) {
            throw new IllegalArgumentException(
                    "ContextSplitter: overlapLines must be non-negative, got: " + overlapLines);
        }
        this.maxCharsPerChunk = maxCharsPerChunk;
        this.overlapLines     = overlapLines;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Splits source code into a list of character-budget-safe chunks.
     *
     * <p>If the source fits within {@link #maxCharsPerChunk} characters, a single-element
     * list is returned (no unnecessary splitting). If the source is null or blank, a
     * single-element list containing an empty string is returned.
     *
     * @param sourceCode the concatenated source code to split; null is treated as empty
     * @return a non-null, non-empty list of chunk strings
     */
    public List<String> split(String sourceCode) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return List.of("");
        }

        // Fast path: entire source fits in one chunk.
        if (sourceCode.length() <= maxCharsPerChunk) {
            return List.of(sourceCode);
        }

        String[] lines = sourceCode.split("\n", -1);
        return buildChunks(lines);
    }

    /**
     * Filters a file name or path against the excluded-extensions blocklist.
     *
     * <p>The check is suffix-based and case-insensitive. A file is excluded if its name
     * (or path) ends with any entry in {@link #EXCLUDED_EXTENSIONS}.
     *
     * @param fileName the file name or path to test
     * @return {@code true} if the file should be included for evaluation
     */
    public static boolean shouldInclude(String fileName) {
        if (fileName == null || fileName.isBlank()) return false;
        String lower = fileName.toLowerCase();
        for (String ext : EXCLUDED_EXTENSIONS) {
            if (lower.endsWith(ext)) return false;
        }
        return true;
    }

    /**
     * Filters a list of file names, retaining only those that pass {@link #shouldInclude}.
     *
     * @param fileNames the candidate file names; must not be null
     * @return a new list containing only the includable file names
     */
    public static List<String> filterFiles(List<String> fileNames) {
        if (fileNames == null) return List.of();
        return fileNames.stream()
                .filter(ContextSplitter::shouldInclude)
                .toList();
    }

    /**
     * Aggregates a list of intermediate per-chunk {@link CriterionResult} objects for the
     * same criterion into a single synthesised result.
     *
     * <p><b>Aggregation strategy:</b>
     * <ul>
     *   <li>{@code score} — the average (rounded down) of all chunk scores, reflecting that
     *       quality issues anywhere in the codebase should be reflected in the overall score.</li>
     *   <li>{@code maxScore} — taken from the first result (consistent across chunks).</li>
     *   <li>{@code feedback} — concatenated, chunk-prefixed summaries.</li>
     *   <li>{@code issues} — union of all issues from all chunks, de-duplicated.</li>
     * </ul>
     *
     * @param chunkResults the per-chunk results for one criterion; must not be null or empty
     * @return a single aggregated {@link CriterionResult}
     */
    public static CriterionResult aggregateChunkResults(List<CriterionResult> chunkResults) {
        if (chunkResults == null || chunkResults.isEmpty()) {
            throw new IllegalArgumentException(
                    "ContextSplitter.aggregateChunkResults: chunkResults must not be null or empty.");
        }

        if (chunkResults.size() == 1) {
            return chunkResults.get(0);
        }

        // Aggregate score: average (floor) across all chunks.
        int totalScore = chunkResults.stream().mapToInt(CriterionResult::score).sum();
        int avgScore   = totalScore / chunkResults.size();
        int maxScore   = chunkResults.get(0).maxScore();

        // Aggregate feedback: join with chunk labels.
        StringBuilder feedbackBuilder = new StringBuilder();
        for (int i = 0; i < chunkResults.size(); i++) {
            feedbackBuilder.append("[Chunk ").append(i + 1).append("] ")
                           .append(chunkResults.get(i).feedback());
            if (i < chunkResults.size() - 1) feedbackBuilder.append(" | ");
        }

        // Aggregate issues: union, de-duplicated (preserve insertion order).
        List<String> allIssues = new ArrayList<>();
        for (CriterionResult cr : chunkResults) {
            for (String issue : cr.issues()) {
                if (!allIssues.contains(issue)) {
                    allIssues.add(issue);
                }
            }
        }

        String criterion = chunkResults.get(0).criterion();
        return new CriterionResult(criterion, avgScore, maxScore,
                feedbackBuilder.toString(), allIssues);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /** @return the configured maximum characters per chunk. */
    public int getMaxCharsPerChunk() { return maxCharsPerChunk; }

    /** @return the configured number of overlap lines between chunks. */
    public int getOverlapLines()     { return overlapLines; }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Builds the list of chunks from an array of source lines with overlap.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Accumulate lines into a current chunk buffer until adding the next line would
     *       exceed {@code maxCharsPerChunk}.</li>
     *   <li>When the budget is full, save the chunk and back up {@code overlapLines} lines
     *       so they appear again at the start of the next chunk.</li>
     *   <li>The final remaining lines are emitted as the last chunk.</li>
     * </ol>
     *
     * @param lines source split by newline
     * @return list of non-empty chunk strings
     */
    private List<String> buildChunks(String[] lines) {
        List<String>  chunks  = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int           i       = 0;

        while (i < lines.length) {
            String line    = lines[i];
            String lineNl  = line + "\n";

            // If adding this line exceeds the budget and we already have content, flush.
            if (current.length() + lineNl.length() > maxCharsPerChunk && current.length() > 0) {
                chunks.add(current.toString());

                // Compute overlap: step back 'overlapLines' lines from current position.
                int overlapStart = Math.max(0, i - overlapLines);
                current = new StringBuilder();
                for (int j = overlapStart; j < i; j++) {
                    current.append(lines[j]).append("\n");
                }
                // Don't advance i — the current line will be re-evaluated next iteration.
                continue;
            }

            current.append(lineNl);
            i++;
        }

        // Emit whatever remains.
        if (current.length() > 0) {
            chunks.add(current.toString());
        }

        return chunks.isEmpty() ? List.of("") : chunks;
    }
}
