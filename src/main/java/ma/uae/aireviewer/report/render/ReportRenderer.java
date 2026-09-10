package ma.uae.aireviewer.report.render;

import ma.uae.aireviewer.report.model.EvaluationReport;

/** Pattern Strategy : rendu du rapport dans un format. */
public interface ReportRenderer {

    String format();

    String render(EvaluationReport report);
}
