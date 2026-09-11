package ma.uae.aireviewer.analysis.analyzer.deterministic;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.FileType;
import java.util.ArrayList;

/** Analyse deterministe : conventions de structure, presence des fichiers de build. */
public final class ProjectOrganizationAnalyzer extends AbstractAnalyzer {

    public ProjectOrganizationAnalyzer(Criterion criterion) {
        super(criterion);
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        long build = files.stream().filter(file -> file.type() == FileType.BUILD_MAVEN
                || file.type() == FileType.BUILD_GRADLE).count();
        long docs = files.stream().filter(file -> file.type() == FileType.DOCUMENTATION).count();
        long config = files.stream().filter(file -> file.type() == FileType.CONFIGURATION).count();
        int score = Math.min(getMax(), (build > 0 ? 4 : 0) + (docs > 0 ? 3 : 0)
                + (config > 0 ? 2 : 0) + (context.project().root().children().size() > 1 ? 1 : 0));
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        if (build > 0) strengths.add("Fichier de construction present");
        else weaknesses.add("Aucun fichier de construction reconnu");
        if (docs > 0) strengths.add("Documentation presente");
        else weaknesses.add("README ou documentation absent");
        return new CriterionResult(criterion().id(), score, getMax(), strengths, weaknesses,
                List.of("Ajouter une structure de modules et une documentation de demarrage si necessaire"),
                ma.uae.aireviewer.analysis.result.ResultStatus.COMPLETED, null);
    }

    private int getMax() {
        return criterion().maxScore();
    }
}
