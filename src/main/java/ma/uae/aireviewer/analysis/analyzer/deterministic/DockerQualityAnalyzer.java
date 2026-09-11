package ma.uae.aireviewer.analysis.analyzer.deterministic;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.FileType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

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
        int points = 0;
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        for (FileNode file : files) {
            try {
                String dockerfile = Files.readString(file.path(), StandardCharsets.UTF_8);
                if (dockerfile.matches("(?s).*\\bUSER\\s+(?!root\\b)\\S+.*")) points += 3;
                if (dockerfile.matches("(?s).*FROM\\s+\\S+:[^\\s]+.*")) points += 3;
                if (dockerfile.indexOf("FROM ") != dockerfile.lastIndexOf("FROM ")) points += 2;
                if (dockerfile.contains("HEALTHCHECK")) points += 1;
            } catch (IOException ignored) {
                weaknesses.add("Dockerfile illisible : " + file.name());
            }
        }
        int score = Math.min(criterion().maxScore(), points);
        if (score >= 5) strengths.add("Le Dockerfile applique plusieurs bonnes pratiques");
        if (score < 5) weaknesses.add("Dockerfile incomplet ou insuffisamment durci");
        return new CriterionResult(criterion().id(), score, criterion().maxScore(), strengths,
                weaknesses, List.of("Utiliser un utilisateur non root et une image versionnee"),
                ma.uae.aireviewer.analysis.result.ResultStatus.COMPLETED, null);
    }
}
