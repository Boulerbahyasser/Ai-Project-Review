package ma.uae.aireviewer.analysis.analyzer.deterministic;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;

/** Analyse deterministe : conventions de structure, presence des fichiers de build. */
public final class ProjectOrganizationAnalyzer extends AbstractAnalyzer {

    public ProjectOrganizationAnalyzer(Criterion criterion) {
        super(criterion);
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        throw new UnsupportedOperationException("TODO : verifier arborescence standard, fichier de build, README");
    }
}
