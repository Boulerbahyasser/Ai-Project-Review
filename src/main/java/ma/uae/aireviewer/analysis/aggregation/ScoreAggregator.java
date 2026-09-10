package ma.uae.aireviewer.analysis.aggregation;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.CriterionProfile;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.analysis.result.OverallScore;

/** Pattern Strategy : regle de consolidation des scores par critere. */
public interface ScoreAggregator {

    OverallScore aggregate(List<CriterionResult> results, CriterionProfile profile);
}
