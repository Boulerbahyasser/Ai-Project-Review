package ma.uae.aireviewer.report.model;

import java.util.List;

/** Rapport en representation Java, independant du format de sortie. */
public record EvaluationReport(
        ReportHeader header,
        ScoreTable scoreTable,
        List<ReportSection> sections,
        String summary) {

    public EvaluationReport {
        sections = List.copyOf(sections);
    }
}
