package ma.uae.aireviewer.report.render;

import ma.uae.aireviewer.report.latex.LatexEscaper;
import ma.uae.aireviewer.report.latex.LatexTemplate;
import ma.uae.aireviewer.report.model.EvaluationReport;

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
        throw new UnsupportedOperationException("TODO");
    }
}
