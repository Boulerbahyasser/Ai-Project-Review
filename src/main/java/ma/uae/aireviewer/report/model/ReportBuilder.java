package ma.uae.aireviewer.report.model;

import java.util.ArrayList;
import java.util.List;

/** Pattern Builder : construction progressive du rapport. */
public final class ReportBuilder {

    private ReportHeader header;
    private ScoreTable scoreTable;
    private final List<ReportSection> sections = new ArrayList<>();
    private String summary = "";

    public ReportBuilder header(ReportHeader header) {
        this.header = header;
        return this;
    }

    public ReportBuilder scoreTable(ScoreTable scoreTable) {
        this.scoreTable = scoreTable;
        return this;
    }

    public ReportBuilder section(ReportSection section) {
        if (section == null) throw new IllegalArgumentException("section ne peut pas etre nulle");
        this.sections.add(section);
        return this;
    }

    public ReportBuilder sections(List<ReportSection> sections) {
        this.sections.clear();
        if (sections != null) this.sections.addAll(sections);
        return this;
    }

    public ReportBuilder summary(String summary) {
        this.summary = summary;
        return this;
    }

    public EvaluationReport build() {
        if (header == null) throw new IllegalStateException("header est obligatoire");
        if (scoreTable == null) throw new IllegalStateException("scoreTable est obligatoire");
        return new EvaluationReport(header, scoreTable, sections, summary == null ? "" : summary);
    }
}
