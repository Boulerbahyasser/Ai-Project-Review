package ma.uae.aireviewer.analysis.result;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.CriterionId;

/**
 * Resultat d'un critere. Representation Java independante du LLM et du format
 * de rapport (cahier des charges, section 7).
 */
public record CriterionResult(
        CriterionId criterion,
        int score,
        int maxScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations,
        ResultStatus status,
        String errorMessage) {

    public CriterionResult {
        strengths = List.copyOf(strengths);
        weaknesses = List.copyOf(weaknesses);
        recommendations = List.copyOf(recommendations);
    }

    public static CriterionResult failed(CriterionId criterion, int maxScore, String errorMessage) {
        return new CriterionResult(criterion, 0, maxScore, List.of(), List.of(), List.of(),
                ResultStatus.FAILED, errorMessage);
    }

    public static CriterionResult skipped(CriterionId criterion, int maxScore, String reason) {
        return new CriterionResult(criterion, 0, maxScore, List.of(), List.of(), List.of(),
                ResultStatus.SKIPPED, reason);
    }
}
