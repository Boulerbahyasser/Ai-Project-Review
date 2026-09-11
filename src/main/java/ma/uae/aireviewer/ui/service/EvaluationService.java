package ma.uae.aireviewer.ui.service;

import com.aireview.llm.CriterionResult;
import com.aireview.llm.EvaluationCriterion;
import com.aireview.llm.EvaluationResult;
import com.aireview.llm.LLMException;
import com.aireview.llm.LLMProvider;
import com.aireview.llm.LatexReportBuilder;
import com.aireview.llm.ResilientEvaluator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import ma.uae.aireviewer.security.archive.ArchiveExtractor;
import ma.uae.aireviewer.security.archive.ExtractionResult;
import ma.uae.aireviewer.security.archive.ExtractionStatus;

/**
 * Couche de liaison entre l'interface graphique et le sous-systeme LLM.
 *
 * <p>Elle n'evalue rien : l'analyse, la resilience, la validation et la
 * generation LaTeX appartiennent toutes a {@code com.aireview.llm}. Son role se
 * limite a trois traductions que l'interface ne doit pas porter :
 * <ul>
 *   <li>un repertoire choisi a la souris devient le code source concatene
 *       qu'attend {@link ResilientEvaluator} ;</li>
 *   <li>les criteres coches deviennent des appels successifs, ce qui rend la
 *       progression observable ;</li>
 *   <li>un resultat devient un fichier, via {@link LatexReportBuilder}.</li>
 * </ul>
 */
public final class EvaluationService {

    /** Repertoires jamais explores : ils gonflent le contexte sans rien apporter. */
    private static final List<String> IGNORED = List.of(
            ".git", "target", "build", "out", "node_modules", ".idea", ".mvn");

    /** Plafond de contexte : un projet entier ne tient pas dans une requete. */
    private static final int MAX_SOURCE_CHARS = 40_000;

    private final LLMProvider provider;
    private final String model;
    private final Path reportDirectory;
    private final ArchiveExtractor archiveExtractor;
    private final List<EvaluationResult> history = new ArrayList<>();

    public EvaluationService(LLMProvider provider, String model, Path reportDirectory,
                              ArchiveExtractor archiveExtractor) {
        this.provider = provider;
        this.model = model;
        this.reportDirectory = reportDirectory;
        this.archiveExtractor = archiveExtractor;
    }

    public String getModel() {
        return model;
    }

    // ------------------------------------------------------------------ import

    /** Parcourt le repertoire et prepare le code source a soumettre. */
    public ProjectSnapshot loadProject(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Repertoire introuvable : " + directory);
        }

        List<Path> sources = collectJavaFiles(directory);
        if (sources.isEmpty()) {
            throw new IllegalStateException(
                    "Aucun fichier .java trouve dans " + directory.getFileName());
        }

        return new ProjectSnapshot(
                directory.getFileName().toString(),
                directory,
                sources,
                concatenate(sources, directory));
    }

    /**
     * Extrait l'archive dans un repertoire temporaire jetable puis delegue a
     * {@link #loadProject(Path)} : au-dela de l'extraction, un projet importe en
     * `.zip` suit exactement le meme chemin qu'un projet importe par repertoire.
     */
    public ProjectSnapshot loadProjectFromArchive(Path archive) {
        if (archive == null || !Files.isRegularFile(archive)) {
            throw new IllegalArgumentException("Archive introuvable : " + archive);
        }

        Path destination;
        try {
            destination = Files.createTempDirectory("aireviewer-import-");
        } catch (IOException cannotCreate) {
            throw new UncheckedIOException(
                    "Impossible de creer un repertoire temporaire pour l'extraction",
                    cannotCreate);
        }

        ExtractionResult extraction = archiveExtractor.extract(archive, destination);
        if (!extraction.success()) {
            String hint = extraction.status() == ExtractionStatus.DOCKER_UNAVAILABLE
                    ? "\n\nDemarrez Docker Desktop puis reessayez, ou selectionnez le projet "
                            + "deja decompresse via \"Select Project\"."
                    : "";
            throw new IllegalStateException(
                    "Extraction de l'archive impossible (" + extraction.status() + ") : "
                            + extraction.message() + hint);
        }

        return loadProject(destination);
    }

    private List<Path> collectJavaFiles(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !isIgnored(root, path))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException unreadable) {
            throw new UncheckedIOException("Lecture impossible de " + root, unreadable);
        }
    }

    private boolean isIgnored(Path root, Path file) {
        for (Path segment : root.relativize(file)) {
            if (IGNORED.contains(segment.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Concatene les sources en prefixant chaque fichier de son chemin relatif :
     * sans ce repere, le modele ne peut pas situer ses observations.
     */
    private String concatenate(List<Path> sources, Path root) {
        StringBuilder code = new StringBuilder();
        for (Path file : sources) {
            if (code.length() >= MAX_SOURCE_CHARS) {
                code.append("// [contexte tronque : ")
                        .append(sources.size())
                        .append(" fichiers au total]\n");
                break;
            }
            code.append("// ===== ").append(root.relativize(file)).append(" =====\n")
                    .append(read(file))
                    .append("\n\n");
        }
        return code.toString();
    }

    private String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException unreadable) {
            return "// [fichier illisible]";
        }
    }

    // ----------------------------------------------------------------- analyse

    /**
     * Evalue les criteres retenus, un appel par critere afin que la progression
     * soit observable. Chaque appel delegue integralement a
     * {@link ResilientEvaluator} : reessais, validation et tolerance aux pannes
     * restent de son ressort.
     */
    public EvaluationResult evaluate(ProjectSnapshot project,
                                     List<EvaluationCriterion> criteria,
                                     ProgressCallback progress) throws LLMException {

        ResilientEvaluator evaluator = new ResilientEvaluator(provider);
        List<CriterionResult> collected = new ArrayList<>();
        long startedAt = System.currentTimeMillis();

        for (int index = 0; index < criteria.size(); index++) {
            EvaluationCriterion criterion = criteria.get(index);
            progress.report(index, criteria.size(),
                    "Evaluation : " + criterion.getDisplayName());

            EvaluationResult single = evaluator.evaluate(
                    project.name(), project.sourceCode(), model, List.of(criterion));
            collected.addAll(single.results());

            progress.report(index + 1, criteria.size(),
                    criterion.getDisplayName() + " termine");
        }

        EvaluationResult result = new EvaluationResult(
                project.name(),
                collected,
                collected.stream().mapToInt(CriterionResult::score).sum(),
                collected.stream().mapToInt(CriterionResult::maxScore).sum(),
                System.currentTimeMillis() - startedAt);

        history.add(0, result);
        return result;
    }

    // ----------------------------------------------------------------- rapport

    /** Produit le fichier LaTeX via le constructeur de rapport du sous-systeme. */
    public Path generateReport(EvaluationResult result) throws IOException {
        Files.createDirectories(reportDirectory);

        return LatexReportBuilder.forResult(result)
                .withModel(model)
                .withAnalysisDate(LocalDate.now().toString())
                .withOutputPath(reportDirectory.resolve("evaluation.tex"))
                .buildAndWrite();
    }

    public List<EvaluationResult> getHistory() {
        return List.copyOf(history);
    }
}
