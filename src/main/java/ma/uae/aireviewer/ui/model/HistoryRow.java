package ma.uae.aireviewer.ui.model;

import com.aireview.llm.EvaluationResult;

/** Ligne de l'historique, adaptee d'un EvaluationResult du sous-systeme LLM. */
public final class HistoryRow {

    private final EvaluationResult result;
    private final String model;
    private final String date;

    public HistoryRow(EvaluationResult result, String model, String date) {
        this.result = result;
        this.model = model;
        this.date = date;
    }

    public String getProject() {
        return result.projectName();
    }

    public String getDate() {
        return date;
    }

    public String getScore() {
        return String.format("%.0f%%", result.overallPercent());
    }

    public String getModel() {
        return model;
    }

    public EvaluationResult getResult() {
        return result;
    }
}
