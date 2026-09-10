package ma.uae.aireviewer.analysis.analyzer.ai;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;

/**
 * Critere HYBRID : combine une mesure deterministe et une appreciation par LLM
 * (cahier des charges, section 1 : combiner analyses deterministes et generees).
 */
public final class HybridCriterionAnalyzer extends AbstractAnalyzer {

    private final Analyzer deterministicPart;
    private final Analyzer llmPart;

    public HybridCriterionAnalyzer(Criterion criterion, Analyzer deterministicPart, Analyzer llmPart) {
        super(criterion);
        this.deterministicPart = deterministicPart;
        this.llmPart = llmPart;
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        throw new UnsupportedOperationException(
                "TODO : executer les deux analyses puis fusionner scores et constats");
    }
}
