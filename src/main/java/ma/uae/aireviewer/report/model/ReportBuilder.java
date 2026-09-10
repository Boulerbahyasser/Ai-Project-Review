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
        this.sections.add(section);
        return this;
    }

    public ReportBuilder summary(String summary) {
        this.summary = summary;
        return this;
    }

    public EvaluationReport build() {
        throw new UnsupportedOperationException("TODO : verifier les champs obligatoires");
    }
}
