package ma.uae.aireviewer.analysis.analyzer.deterministic;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.FileType;

/** Analyse deterministe : proportion de fichiers de test par rapport aux sources. */
public final class TestPresenceAnalyzer extends AbstractAnalyzer {

    public TestPresenceAnalyzer(Criterion criterion) {
        super(criterion);
    }

    @Override
    protected List<FileNode> relevantFiles(AnalysisContext context) {
        return context.selectedFiles().stream()
                .filter(file -> file.type() == FileType.JAVA || file.type() == FileType.TEST)
                .toList();
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        throw new UnsupportedOperationException("TODO : calculer le ratio tests/sources et le convertir en score");
    }
}
