package ma.uae.aireviewer.analysis.aggregation;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.criterion.CriterionProfile;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.analysis.result.OverallScore;
import ma.uae.aireviewer.analysis.result.ResultStatus;

/** Moyenne ponderee par le poids declare de chaque critere ; les criteres en echec sont exclus. */
public final class WeightedAverageAggregator implements ScoreAggregator {

    @Override
    public OverallScore aggregate(List<CriterionResult> results, CriterionProfile profile) {
        double score = 0;
        double max = 0;
        for (CriterionResult result : results) {
            if (result.status() == ResultStatus.FAILED || result.status() == ResultStatus.SKIPPED) {
                continue;
            }
            double weight = profile.find(result.criterion()).map(Criterion::weight).orElse(1.0);
            score += result.score() * weight;
            max += result.maxScore() * weight;
        }
        return new OverallScore(score, max);
    }
}
