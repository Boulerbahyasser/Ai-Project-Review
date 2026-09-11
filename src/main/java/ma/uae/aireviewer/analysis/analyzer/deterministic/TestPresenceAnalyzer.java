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
        long tests = files.stream().filter(file -> file.type() == FileType.TEST).count();
        long sources = files.stream().filter(file -> file.type() == FileType.JAVA).count();
        double ratio = sources == 0 ? (tests > 0 ? 1 : 0) : (double) tests / sources;
        int score = (int) Math.round(Math.min(1.0, ratio) * criterion().maxScore());
        return new CriterionResult(criterion().id(), score, criterion().maxScore(),
                tests > 0 ? List.of("Des fichiers de test sont presents") : List.of(),
                tests == 0 ? List.of("Aucun fichier de test reconnu") : List.of(),
                tests == 0 ? List.of("Ajouter des tests unitaires et d'integration") : List.of(),
                ma.uae.aireviewer.analysis.result.ResultStatus.COMPLETED, null);
    }
}
