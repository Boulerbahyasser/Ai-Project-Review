package ma.uae.aireviewer.report.assembly;

import ma.uae.aireviewer.analysis.result.AnalysisResult;
import ma.uae.aireviewer.report.model.EvaluationReport;
import ma.uae.aireviewer.report.model.ReportBuilder;
import ma.uae.aireviewer.report.model.ReportHeader;
import ma.uae.aireviewer.report.model.ReportSection;
import ma.uae.aireviewer.report.model.ScoreRow;
import ma.uae.aireviewer.report.model.ScoreTable;
import java.util.ArrayList;
import java.util.List;

/** Assemblage par defaut : en-tete, tableau des scores, une section par critere. */
public final class DefaultReportAssembler implements ReportAssembler {

    @Override
    public EvaluationReport assemble(AnalysisResult result) {
        List<ScoreRow> rows = result.criterionResults().stream()
                .map(item -> new ScoreRow(item.criterion().toString(), item.score(), item.maxScore()))
                .toList();
        List<ReportSection> sections = new ArrayList<>();
        for (var item : result.criterionResults()) {
            List<String> paragraphs = item.errorMessage() == null ? List.of() : List.of(item.errorMessage());
            sections.add(new ReportSection(item.criterion().toString(), paragraphs,
                    concat(item.strengths(), item.weaknesses(), item.recommendations())));
        }
        return new ReportBuilder()
                .header(new ReportHeader(result.project().name(), result.finishedAt(),
                        "Profil " + result.profileName(), result.profileName(), result.modelUsed()))
                .scoreTable(new ScoreTable(rows, result.overall().score(), result.overall().maxScore()))
                .summary("Score global : " + String.format("%.2f", result.overall().percentage()) + "%")
                .sections(sections)
                .build();
    }

    private static List<String> concat(List<String>... values) {
        return java.util.Arrays.stream(values).flatMap(List::stream).toList();
    }
}
