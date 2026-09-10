package ma.uae.aireviewer.project.model;

import java.nio.file.Path;

/** Feuille du Composite : un fichier du projet analyse. */
public record FileNode(String name, Path path, long sizeInBytes, FileType type) implements ProjectNode {

    @Override
    public <R> R accept(ProjectVisitor<R> visitor) {
        return visitor.visitFile(this);
    }
}
