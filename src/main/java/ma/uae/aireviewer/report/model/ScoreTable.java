package ma.uae.aireviewer.report.model;

import java.util.List;

/** Tableau de synthese des scores. */
public record ScoreTable(List<ScoreRow> rows, double total, double maxTotal) {

    public ScoreTable {
        rows = List.copyOf(rows);
    }
}
