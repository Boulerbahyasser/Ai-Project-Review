package ma.uae.aireviewer.ui.mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import javafx.scene.control.TreeItem;

/**
 * Construit l'arborescence affichee a partir du repertoire du projet.
 *
 * <p>Seul endroit ou le systeme de fichiers rencontre JavaFX : les vues
 * n'explorent jamais le projet analyse elles-memes.
 */
public final class ProjectTreeMapper {

    private static final List<String> IGNORED = List.of(
            ".git", "target", "build", "out", "node_modules", ".idea", ".mvn");

    public TreeItem<String> toTreeItem(Path path) {
        boolean directory = Files.isDirectory(path);
        TreeItem<String> item = new TreeItem<>(
                directory ? path.getFileName() + "/" : path.getFileName().toString());

        if (!directory) {
            return item;
        }

        for (Path child : childrenOf(path)) {
            item.getChildren().add(toTreeItem(child));
        }
        return item;
    }

    private List<Path> childrenOf(Path directory) {
        try (var entries = Files.list(directory)) {
            return entries
                    .filter(path -> !IGNORED.contains(path.getFileName().toString()))
                    .sorted(Comparator
                            .comparing((Path path) -> Files.isDirectory(path) ? 0 : 1)
                            .thenComparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();
        } catch (IOException unreadable) {
            // Un repertoire illisible est simplement absent de l'affichage.
            return List.of();
        }
    }
}
