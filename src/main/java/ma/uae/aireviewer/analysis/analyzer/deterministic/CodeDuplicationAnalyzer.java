package ma.uae.aireviewer.analysis.analyzer.deterministic;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/** Analyse deterministe : detection de duplication par empreintes de blocs de lignes. */
public final class CodeDuplicationAnalyzer extends AbstractAnalyzer {

    public CodeDuplicationAnalyzer(Criterion criterion) {
        super(criterion);
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        Map<String, Integer> windows = new HashMap<>();
        int total = 0;
        for (FileNode file : files) {
            try {
                String[] lines = Files.readString(file.path(), StandardCharsets.UTF_8).lines()
                        .map(String::trim).filter(line -> !line.isBlank()).toArray(String[]::new);
                for (int i = 0; i + 4 <= lines.length; i++) {
                    String key = String.join("\n", java.util.Arrays.copyOfRange(lines, i, i + 4));
                    windows.merge(key, 1, Integer::sum);
                    total++;
                }
            } catch (IOException ignored) {
                // unreadable files do not contribute to the heuristic
            }
        }
        int duplicateWindows = windows.values().stream().mapToInt(value -> Math.max(0, value - 1)).sum();
        double duplication = total == 0 ? 0 : (double) duplicateWindows / total;
        int score = (int) Math.round((1 - Math.min(1, duplication)) * criterion().maxScore());
        return new CriterionResult(criterion().id(), score, criterion().maxScore(),
                duplication < .1 ? List.of("Peu de blocs dupliques detectes") : List.of(),
                duplication >= .1 ? List.of("Des blocs de code repetes ont ete detectes") : List.of(),
                duplication >= .1 ? List.of("Extraire les comportements communs") : List.of(),
                ma.uae.aireviewer.analysis.result.ResultStatus.COMPLETED, null);
    }
}
