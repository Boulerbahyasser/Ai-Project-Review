package ma.uae.aireviewer.analysis.analyzer;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;

/**
 * Pattern Template Method : fixe le deroulement commun d'une analyse
 * (selection des fichiers pertinents -> evaluation -> resultat) et laisse
 * les sous-classes specialiser les etapes variables.
 */
public abstract class AbstractAnalyzer implements Analyzer {

    private final Criterion criterion;

    protected AbstractAnalyzer(Criterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public final Criterion criterion() {
        return criterion;
    }

    @Override
    public final CriterionResult analyze(AnalysisContext context) {
        List<FileNode> relevant = relevantFiles(context);
        if (relevant.isEmpty()) {
            return CriterionResult.skipped(criterion.id(), criterion.maxScore(),
                    "Aucun fichier pertinent pour ce critere");
        }
        return evaluate(context, relevant);
    }

    /** Etape specialisable : sous-ensemble de fichiers utile a ce critere. */
    protected List<FileNode> relevantFiles(AnalysisContext context) {
        return context.selectedFiles();
    }

    /** Etape obligatoire : evaluation effective du critere. */
    protected abstract CriterionResult evaluate(AnalysisContext context, List<FileNode> files);
}
