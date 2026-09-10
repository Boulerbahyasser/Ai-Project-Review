package ma.uae.aireviewer.analysis.analyzer.deterministic;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;

/** Analyse deterministe : detection de duplication par empreintes de blocs de lignes. */
public final class CodeDuplicationAnalyzer extends AbstractAnalyzer {

    public CodeDuplicationAnalyzer(Criterion criterion) {
        super(criterion);
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        throw new UnsupportedOperationException("TODO : hacher des fenetres de N lignes normalisees et compter les collisions");
    }
}
