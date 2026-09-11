package ma.uae.aireviewer.security.archive;

import ma.uae.aireviewer.configuration.UnzipConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Extraction dans un conteneur Docker dedie, supprime apres usage.
 *
 * <p>Le conteneur n'a pas d'ENTRYPOINT privilegie (cf. docker/unzip/Dockerfile) : le
 * script ci-dessous est construit ici et execute par {@code /bin/sh -c}. Il ne copie
 * rien vers {@code /output} tant que toutes les verifications n'ont pas reussi :
 *
 * <ol>
 *   <li>{@code unzip -Z1} liste les entrees sans rien ecrire ; une archive illisible
 *       s'arrete la (archive invalide) ;</li>
 *   <li>chaque nom est controle avant extraction : chemin absolu, {@code ..} ou
 *       antislash sont rejetes (chemin dangereux) ;</li>
 *   <li>le nombre d'entrees est compare a {@code maxEntryCount} ;</li>
 *   <li>l'extraction reelle se fait dans {@code /work}, un {@code tmpfs} dont la
 *       taille est fixee a {@code maxExtractedSizeMb} : une bombe zip fait echouer
 *       {@code unzip} lui-meme (disque tmpfs plein), au niveau du systeme de
 *       fichiers, pas seulement par une verification applicative ;</li>
 *   <li>apres extraction, tout lien symbolique produit fait rejeter l'archive ;</li>
 *   <li>la taille totale extraite est revérifiée explicitement, en plus de la
 *       limite {@code tmpfs} ;</li>
 *   <li>seul un resultat ayant franchi toutes ces etapes est copie vers
 *       {@code /output}, l'unique montage en ecriture du conteneur.</li>
 * </ol>
 */
public final class DockerArchiveExtractor implements ArchiveExtractor {

    private static final int EXIT_CORRUPT = 10;
    private static final int EXIT_UNSAFE_PATH = 20;
    private static final int EXIT_TOO_MANY_ENTRIES = 21;
    private static final int EXIT_SYMLINK = 22;
    private static final int EXIT_TOO_LARGE = 23;
    private static final int EXIT_DOCKER_FAILURE = 125;

    private final UnzipConfig config;

    public DockerArchiveExtractor(UnzipConfig config) {
        this.config = config;
    }

    @Override
    public ExtractionResult extract(Path archive, Path destination) {
        if (archive == null || !Files.isRegularFile(archive)) {
            return new ExtractionResult(ExtractionStatus.INVALID_ARCHIVE, destination, -1,
                    "Archive introuvable : " + archive);
        }
        try {
            Files.createDirectories(destination);
        } catch (IOException cannotCreate) {
            return new ExtractionResult(ExtractionStatus.INVALID_ARCHIVE, destination, -1,
                    "Repertoire de destination invalide : " + cannotCreate.getMessage());
        }

        List<String> command = buildCommand(archive, destination);
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(false).start();
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            Thread out = Thread.ofVirtual().start(() -> read(process.getInputStream(), stdout));
            Thread err = Thread.ofVirtual().start(() -> read(process.getErrorStream(), stderr));
            boolean completed = process.waitFor(config.timeoutSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                out.join(1000);
                err.join(1000);
                return new ExtractionResult(ExtractionStatus.TIMEOUT, destination, -1,
                        "Extraction interrompue apres " + config.timeoutSeconds() + " s");
            }
            out.join(1000);
            err.join(1000);
            return toResult(process.exitValue(), destination, stderr.toString());
        } catch (IOException dockerUnavailable) {
            return new ExtractionResult(ExtractionStatus.DOCKER_UNAVAILABLE, destination, -1,
                    dockerUnavailable.getMessage());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return new ExtractionResult(ExtractionStatus.TIMEOUT, destination, -1,
                    "Extraction interrompue");
        }
    }

    // -------------------------------------------------------------- docker run

    private List<String> buildCommand(Path archive, Path destination) {
        ArrayList<String> command = new ArrayList<>(List.of(
                "docker", "run", "--rm", "--network", "none", "--read-only",
                "--user", config.user(),
                "--cap-drop", "ALL", "--security-opt", "no-new-privileges",
                "--memory", config.memoryLimit(),
                "--pids-limit", Integer.toString(config.pidsLimit()),
                "--tmpfs", "/work:rw,size=" + config.maxExtractedSizeMb() + "m,"
                        + tmpfsOwnership(),
                "-v", archive.toAbsolutePath() + ":/input/archive.zip:ro",
                "-v", destination.toAbsolutePath() + ":/output:rw",
                config.dockerImage(), extractionScript()));
        return command;
    }

    /**
     * Sans uid/gid explicites, Docker monte le tmpfs appartenant a root : l'utilisateur
     * non root de {@code config.user()} (cf. Dockerfile) ne peut alors pas y creer
     * {@code /work/out}. On aligne le proprietaire du tmpfs sur cet utilisateur.
     */
    private String tmpfsOwnership() {
        String[] parts = config.user().split(":", 2);
        String uid = parts[0];
        String gid = parts.length > 1 ? parts[1] : parts[0];
        return "uid=" + uid + ",gid=" + gid + ",mode=0700";
    }

    /** Genere le script execute dans le conteneur ; voir le plan en tete de classe. */
    private String extractionScript() {
        long maxSizeKb = (long) config.maxExtractedSizeMb() * 1024;
        String script = """
                ENTRIES=$(unzip -Z1 /input/archive.zip 2>/dev/null)
                LIST_RC=$?
                if [ "$LIST_RC" -ne 0 ]; then exit __EXIT_CORRUPT__; fi
                if echo "$ENTRIES" | grep -qE '^/|(^|/)\\.\\.(/|$)|\\\\'; then exit __EXIT_UNSAFE_PATH__; fi
                COUNT=$(echo "$ENTRIES" | grep -c .)
                if [ "$COUNT" -gt __MAX_ENTRIES__ ]; then exit __EXIT_TOO_MANY__; fi
                mkdir -p /work/out
                unzip -o -q -d /work/out /input/archive.zip
                EXTRACT_RC=$?
                if [ "$EXTRACT_RC" -eq 50 ]; then exit __EXIT_TOO_LARGE__; fi
                if [ "$EXTRACT_RC" -ne 0 ]; then exit __EXIT_CORRUPT__; fi
                if find /work/out -type l | grep -q .; then exit __EXIT_SYMLINK__; fi
                SIZE_KB=$(du -sk /work/out | cut -f1)
                if [ "$SIZE_KB" -gt __MAX_SIZE_KB__ ]; then exit __EXIT_TOO_LARGE__; fi
                cp -r /work/out/. /output/
                exit 0
                """;
        return script
                .replace("__EXIT_CORRUPT__", Integer.toString(EXIT_CORRUPT))
                .replace("__EXIT_UNSAFE_PATH__", Integer.toString(EXIT_UNSAFE_PATH))
                .replace("__EXIT_TOO_MANY__", Integer.toString(EXIT_TOO_MANY_ENTRIES))
                .replace("__EXIT_TOO_LARGE__", Integer.toString(EXIT_TOO_LARGE))
                .replace("__EXIT_SYMLINK__", Integer.toString(EXIT_SYMLINK))
                .replace("__MAX_ENTRIES__", Integer.toString(config.maxEntryCount()))
                .replace("__MAX_SIZE_KB__", Long.toString(maxSizeKb));
    }

    // ------------------------------------------------------------ exit codes

    private ExtractionResult toResult(int exitCode, Path destination, String stderr) {
        if (exitCode != 0 && looksLikeDockerUnavailable(stderr)) {
            return new ExtractionResult(ExtractionStatus.DOCKER_UNAVAILABLE, destination, exitCode, stderr);
        }
        return switch (exitCode) {
            case 0 -> new ExtractionResult(ExtractionStatus.SUCCESS, destination, 0, "");
            case EXIT_TOO_MANY_ENTRIES, EXIT_TOO_LARGE ->
                    new ExtractionResult(ExtractionStatus.LIMIT_EXCEEDED, destination, exitCode, stderr);
            case EXIT_DOCKER_FAILURE ->
                    new ExtractionResult(ExtractionStatus.DOCKER_UNAVAILABLE, destination, exitCode, stderr);
            default -> new ExtractionResult(ExtractionStatus.INVALID_ARCHIVE, destination, exitCode, stderr);
        };
    }

    /**
     * Le code de sortie de {@code docker run} quand le demon est injoignable n'est
     * pas garanti (125 sur certaines versions, 1 ailleurs) : on reconnait plutot le
     * message d'erreur du client Docker, pour ne jamais confondre ce cas avec une
     * archive corrompue (INVALID_ARCHIVE).
     */
    private static boolean looksLikeDockerUnavailable(String stderr) {
        String lower = stderr.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("cannot connect to the docker daemon")
                || lower.contains("failed to connect to docker api")
                || lower.contains("error during connect")
                || lower.contains("docker daemon is not running")
                || lower.contains("is the docker daemon running")
                || lower.contains("dockerdesktoplinuxengine");
    }

    private static void read(InputStream stream, StringBuilder target) {
        try (stream) {
            target.append(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }
}
