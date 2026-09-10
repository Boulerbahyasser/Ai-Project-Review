package ma.uae.aireviewer.project.selection;

import java.util.List;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.SoftwareProject;

/** Applique une strategie de selection a l'ensemble du projet. */
public final class FileSelector {

    private final FileSelectionStrategy strategy;

    public FileSelector(FileSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    public List<FileNode> select(SoftwareProject project) {
        return project.allFiles().stream().filter(strategy::accepts).toList();
    }
}
