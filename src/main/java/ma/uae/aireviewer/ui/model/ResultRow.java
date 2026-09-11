package ma.uae.aireviewer.ui.model;

import com.aireview.llm.CriterionResult;

/** Ligne du tableau des resultats, adaptee d'un CriterionResult du sous-systeme LLM. */
public final class ResultRow {

    private final CriterionResult result;

    public ResultRow(CriterionResult result) {
        this.result = result;
    }

    public String getCriterion() {
        return result.criterion();
    }

    public String getScore() {
        return result.score() + "/" + result.maxScore();
    }

    public String getStatus() {
        if (!result.isValid()) {
            return "Invalid";
        }
        return result.issues().isEmpty() ? "Clean" : result.issues().size() + " issue(s)";
    }

    public CriterionResult getResult() {
        return result;
    }
}
