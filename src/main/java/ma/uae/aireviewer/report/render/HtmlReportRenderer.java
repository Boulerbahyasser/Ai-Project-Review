package ma.uae.aireviewer.report.render;

import ma.uae.aireviewer.report.model.EvaluationReport;

/** Rendu HTML (extension optionnelle, section 18). */
public final class HtmlReportRenderer implements ReportRenderer {

    @Override
    public String format() {
        return "html";
    }

    @Override
    public String render(EvaluationReport report) {
        throw new UnsupportedOperationException("TODO");
    }
}
