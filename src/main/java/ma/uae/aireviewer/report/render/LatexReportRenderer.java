package ma.uae.aireviewer.report.render;

import ma.uae.aireviewer.report.latex.LatexEscaper;
import ma.uae.aireviewer.report.latex.LatexTemplate;
import ma.uae.aireviewer.report.model.EvaluationReport;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/** Rendu LaTeX, format obligatoire (section 7). */
public final class LatexReportRenderer implements ReportRenderer {

    private final LatexTemplate template;
    private final LatexEscaper escaper;

    public LatexReportRenderer(LatexTemplate template, LatexEscaper escaper) {
        this.template = template;
        this.escaper = escaper;
    }

    @Override
    public String format() {
        return "latex";
    }

    @Override
    public String render(EvaluationReport report) {
        String table = "\\begin{tabular}{lrr}\n\\toprule\nCritere & Score & Maximum \\\\\n\\midrule\n"
                + report.scoreTable().rows().stream()
                .map(row -> escaper.escape(row.criterion()) + " & " + row.score() + " & "
                        + row.maxScore() + " \\\\")
                .collect(Collectors.joining("\n"))
                + "\n\\midrule\nTotal & " + report.scoreTable().total() + " & "
                + report.scoreTable().maxTotal() + " \\\\\n\\bottomrule\n\\end{tabular}";
        String sections = report.sections().stream().map(section -> {
            String bullets = section.bullets().stream()
                    .map(item -> "\\item " + escaper.escape(item))
                    .collect(Collectors.joining("\n"));
            return "\\subsection{" + escaper.escape(section.title()) + "}\n"
                    + section.paragraphs().stream().map(escaper::escape)
                    .collect(Collectors.joining("\n\n"))
                    + (bullets.isBlank() ? "" : "\n\\begin{itemize}\n" + bullets + "\n\\end{itemize}");
        }).collect(Collectors.joining("\n\n"));
        var variables = new LinkedHashMap<String, String>();
        variables.put("projectName", escaper.escape(report.header().projectName()));
        variables.put("analysisDate", escaper.escape(String.valueOf(report.header().analysisDate())));
        variables.put("configurationSummary", escaper.escape(report.header().configurationSummary()));
        variables.put("modelUsed", escaper.escape(report.header().modelUsed()));
        variables.put("scoreTable", table);
        variables.put("sections", sections);
        variables.put("summary", escaper.escape(report.summary()));
        return template.render(variables);
    }
}
