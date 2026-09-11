package com.aireview.llm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * PATTERN: Builder
 *
 * <p>{@code LatexReportBuilder} produces a fully self-contained LaTeX document
 * ({@code .tex}) from an {@link EvaluationResult}.  It is the only class in the LLM
 * subsystem that understands the LaTeX serialisation format — all other components
 * deal exclusively in Java objects.  This strict separation means the evaluation
 * pipeline never needs to know <em>how</em> the data will be rendered; it only
 * produces {@link EvaluationResult} values.
 *
 * <h2>Architectural separation</h2>
 * <pre>
 *   LLMProvider  →  ResilientEvaluator  →  EvaluationResult
 *                                                  ↓
 *                                      LatexReportBuilder   ← TEXT only here
 *                                                  ↓
 *                                           evaluation.tex
 * </pre>
 *
 * <h2>Design choices</h2>
 * <ul>
 *   <li><b>Builder pattern:</b> All optional parameters (output path, model name,
 *       analysis date) are set via a fluent builder so callers only specify what they
 *       care about.  The actual construction is confined to {@link #build()}.</li>
 *   <li><b>LaTeX injection defence:</b> Every string sourced from LLM output (criterion
 *       name, feedback text, issue text) is passed through {@link #escape(String)}
 *       before being embedded in the document.  This prevents accidental LaTeX command
 *       injection via special characters such as {@code _}, {@code %}, {@code &},
 *       {@code $}, {@code #}, {@code ^}, {@code ~}, {@code \}, {@code {}, and
 *       {@code }}.</li>
 *   <li><b>Self-contained document:</b> The generated file includes a minimal
 *       preamble so it can be compiled with {@code pdflatex} without any external
 *       style files.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 *   LatexReportBuilder.forResult(evaluationResult)
 *           .withModel("gemma2:2b")
 *           .withOutputPath(Path.of("evaluation.tex"))
 *           .buildAndWrite();
 * </pre>
 */
public final class LatexReportBuilder {

    // -----------------------------------------------------------------------
    // Builder fields
    // -----------------------------------------------------------------------

    private final EvaluationResult result;
    private       String           model       = "unknown";
    private       String           analysisDate;
    private       Path             outputPath  = Path.of("evaluation.tex");

    // -----------------------------------------------------------------------
    // PATTERN: Builder — private constructor, static factory entry point
    // -----------------------------------------------------------------------

    private LatexReportBuilder(EvaluationResult result) {
        this.result       = Objects.requireNonNull(result,
                "LatexReportBuilder: result must not be null.");
        this.analysisDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    /**
     * Creates a new builder for the given {@link EvaluationResult}.
     *
     * @param result the evaluation to render; must not be null
     * @return a fresh {@code LatexReportBuilder} instance
     */
    public static LatexReportBuilder forResult(EvaluationResult result) {
        return new LatexReportBuilder(result);
    }

    // -----------------------------------------------------------------------
    // Fluent setters
    // -----------------------------------------------------------------------

    /**
     * Sets the model identifier printed in the report header.
     *
     * @param model e.g. {@code "gemma2:2b"}; null is silently treated as "unknown"
     * @return this builder (fluent)
     */
    public LatexReportBuilder withModel(String model) {
        this.model = (model == null || model.isBlank()) ? "unknown" : model.strip();
        return this;
    }

    /**
     * Overrides the analysis date shown in the document.  By default the current
     * system date is used.
     *
     * @param date a non-null, non-blank date string (any human-readable format)
     * @return this builder (fluent)
     */
    public LatexReportBuilder withAnalysisDate(String date) {
        if (date != null && !date.isBlank()) {
            this.analysisDate = date.strip();
        }
        return this;
    }

    /**
     * Sets the filesystem path where the generated {@code .tex} file should be written.
     *
     * @param path output path; must not be null
     * @return this builder (fluent)
     */
    public LatexReportBuilder withOutputPath(Path path) {
        this.outputPath = Objects.requireNonNull(path,
                "LatexReportBuilder: outputPath must not be null.");
        return this;
    }

    // -----------------------------------------------------------------------
    // Build methods
    // -----------------------------------------------------------------------

    /**
     * Assembles and returns the complete LaTeX document as a {@code String}.
     *
     * <p>No I/O is performed; use {@link #buildAndWrite()} to also write the file.
     *
     * @return the complete document content; never null or blank
     */
    public String build() {
        StringBuilder sb = new StringBuilder();

        appendPreamble(sb);
        appendTitle(sb);
        appendMetadataTable(sb);
        appendSummaryTable(sb);
        appendDetailedResults(sb);
        appendPostamble(sb);

        return sb.toString();
    }

    /**
     * Calls {@link #build()} and writes the result to {@link #outputPath}.
     *
     * @return the path the file was written to (equal to {@link #outputPath})
     * @throws IOException if the file cannot be created or written
     */
    public Path buildAndWrite() throws IOException {
        String content = build();
        Files.writeString(outputPath, content);
        return outputPath;
    }

    // -----------------------------------------------------------------------
    // Document section builders (private)
    // -----------------------------------------------------------------------

    /** LaTeX preamble: document class, packages, colour definitions. */
    private void appendPreamble(StringBuilder sb) {
        sb.append("% Auto-generated by LatexReportBuilder — do not edit manually.\n");
        sb.append("\\documentclass[11pt,a4paper]{article}\n");
        sb.append("\\usepackage[T1]{fontenc}\n");
        sb.append("\\usepackage[utf8]{inputenc}\n");
        sb.append("\\usepackage{lmodern}\n");
        sb.append("\\usepackage{geometry}\n");
        sb.append("\\usepackage{booktabs}\n");
        sb.append("\\usepackage{xcolor}\n");
        sb.append("\\usepackage{enumitem}\n");
        sb.append("\\usepackage{parskip}\n");
        sb.append("\\usepackage{hyperref}\n");
        sb.append("\\geometry{margin=2.5cm}\n");
        sb.append("\\definecolor{passgreen}{HTML}{2DA44E}\n");
        sb.append("\\definecolor{warnred}{HTML}{CF222E}\n");
        sb.append("\n");
    }

    /** Title block: project name, date, model. */
    private void appendTitle(StringBuilder sb) {
        sb.append("\\title{\\textbf{AI Project Evaluation Report}\\\\\n");
        sb.append("        \\large Project: \\texttt{")
          .append(escape(result.projectName()))
          .append("}}\n");
        sb.append("\\author{AI Project Reviewer\\\\")
          .append("Model: \\texttt{").append(escape(model)).append("}}\n");
        sb.append("\\date{").append(escape(analysisDate)).append("}\n");
        sb.append("\\begin{document}\n");
        sb.append("\\maketitle\n");
        sb.append("\\tableofcontents\n");
        sb.append("\\newpage\n\n");
    }

    /** Small metadata table: project, date, model, total score, duration. */
    private void appendMetadataTable(StringBuilder sb) {
        sb.append("\\section{Evaluation Metadata}\n\n");
        sb.append("\\begin{tabular}{ll}\n");
        sb.append("\\toprule\n");
        sb.append("\\textbf{Field} & \\textbf{Value} \\\\\n");
        sb.append("\\midrule\n");
        sb.append("Project        & \\texttt{").append(escape(result.projectName())).append("} \\\\\n");
        sb.append("Analysis Date  & ").append(escape(analysisDate)).append(" \\\\\n");
        sb.append("Model Used     & \\texttt{").append(escape(model)).append("} \\\\\n");
        sb.append("Total Score    & ").append(result.totalScore())
          .append(" / ").append(result.maxTotalScore())
          .append(String.format(" (%.1f\\%%)", result.overallPercent()))
          .append(" \\\\\n");
        sb.append("Criteria Count & ").append(result.results().size()).append(" \\\\\n");
        sb.append("Duration       & ").append(result.durationMillis()).append(" ms \\\\\n");
        sb.append("\\bottomrule\n");
        sb.append("\\end{tabular}\n\n");
    }

    /**
     * Summary table: Criterion | Score | Maximum | Percentage.
     * PATTERN: Builder — structured column-by-column for readability.
     */
    private void appendSummaryTable(StringBuilder sb) {
        sb.append("\\section{Score Summary}\n\n");
        sb.append("\\begin{tabular}{lrrr}\n");
        sb.append("\\toprule\n");
        sb.append("\\textbf{Criterion} & \\textbf{Score} & \\textbf{Maximum} & \\textbf{Percentage} \\\\\n");
        sb.append("\\midrule\n");

        for (CriterionResult cr : result.results()) {
            double pct    = cr.scorePercent();
            String colour = pct >= 70.0 ? "passgreen" : "warnred";
            sb.append(escape(cr.criterion()))
              .append(" & ").append(cr.score())
              .append(" & ").append(cr.maxScore())
              .append(" & ").append(String.format("\\textcolor{%s}{%.0f\\%%}", colour, pct))
              .append(" \\\\\n");
        }

        sb.append("\\midrule\n");
        sb.append("\\textbf{Total}")
          .append(" & \\textbf{").append(result.totalScore()).append("}")
          .append(" & \\textbf{").append(result.maxTotalScore()).append("}")
          .append(String.format(" & \\textbf{%.0f\\%%}", result.overallPercent()))
          .append(" \\\\\n");
        sb.append("\\bottomrule\n");
        sb.append("\\end{tabular}\n\n");
    }

    /** Per-criterion detail sections: feedback + issues list. */
    private void appendDetailedResults(StringBuilder sb) {
        sb.append("\\section{Detailed Criterion Results}\n\n");

        for (CriterionResult cr : result.results()) {
            sb.append("\\subsection{").append(escape(cr.criterion())).append("}\n\n");
            sb.append("\\textbf{Score:} ")
              .append(cr.score()).append(" / ").append(cr.maxScore())
              .append(String.format(" (%.0f\\%%)", cr.scorePercent()))
              .append("\n\n");
            sb.append("\\textbf{Feedback:} ")
              .append(escape(cr.feedback()))
              .append("\n\n");

            if (!cr.issues().isEmpty()) {
                sb.append("\\textbf{Issues identified:}\n");
                sb.append("\\begin{itemize}[noitemsep]\n");
                for (String issue : cr.issues()) {
                    sb.append("    \\item ").append(escape(issue)).append("\n");
                }
                sb.append("\\end{itemize}\n\n");
            }
        }
    }

    /** Document postamble. */
    private void appendPostamble(StringBuilder sb) {
        sb.append("\\end{document}\n");
    }

    // -----------------------------------------------------------------------
    // LaTeX character escaping
    // -----------------------------------------------------------------------

    /**
     * Escapes special LaTeX characters in a raw string.
     *
     * <p><b>Security contract:</b> All text originating from LLM output (criterion names,
     * feedback, issue strings) and from untrusted fields (project name) MUST be passed
     * through this method before being embedded in the generated document.  Without this
     * guard, a response containing {@code _}, {@code %}, or {@code \} would break LaTeX
     * compilation or — in adversarial cases — silently alter the rendered output.
     *
     * <p>A sentinel technique prevents double-escaping: backslashes are first replaced
     * with a NUL-delimited marker, all other substitutions run, then the marker is
     * replaced with {@code \textbackslash{}}.  This prevents the brace-escaping pass
     * from corrupting the backslash escape sequence.
     *
     * @param text raw input string; null is treated as empty
     * @return a LaTeX-safe string
     */
    static String escape(String text) {
        if (text == null) return "";

        final String SENTINEL = "\u0000BACKSLASH\u0000";

        return text
                // Step 1 — mark real backslashes with a sentinel
                .replace("\\", SENTINEL)
                // Step 2 — escape all other LaTeX-special characters
                .replace("&",  "\\&")
                .replace("%",  "\\%")
                .replace("$",  "\\$")
                .replace("#",  "\\#")
                .replace("_",  "\\_")
                .replace("{",  "\\{")
                .replace("}",  "\\}")
                .replace("^",  "\\textasciicircum{}")
                .replace("~",  "\\textasciitilde{}")
                .replace("<",  "\\textless{}")
                .replace(">",  "\\textgreater{}")
                // Normalise curly/typographic quotes to ASCII so pdflatex doesn't choke
                .replace("\u2018", "'").replace("\u2019", "'")
                .replace("\u201C", "``").replace("\u201D", "''")
                // Step 3 — resolve sentinel to final LaTeX-safe backslash command
                .replace(SENTINEL, "\\textbackslash{}");
    }
}
