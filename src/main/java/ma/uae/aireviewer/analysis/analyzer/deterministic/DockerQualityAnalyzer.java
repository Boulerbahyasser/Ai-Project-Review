package ma.uae.aireviewer.analysis.analyzer.deterministic;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.FileType;

/** Analyse deterministe : qualite du Dockerfile (image de base, utilisateur non root, etapes). */
public final class DockerQualityAnalyzer extends AbstractAnalyzer {

    public DockerQualityAnalyzer(Criterion criterion) {
        super(criterion);
    }

    @Override
    protected List<FileNode> relevantFiles(AnalysisContext context) {
        return context.selectedFiles().stream().filter(file -> file.type() == FileType.DOCKER).toList();
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        throw new UnsupportedOperationException("TODO : verifier USER non root, tag d'image fixe, build multi-etapes");
    }
}
