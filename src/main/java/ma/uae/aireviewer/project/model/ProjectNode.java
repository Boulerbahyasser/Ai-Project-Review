package ma.uae.aireviewer.project.model;

import java.nio.file.Path;

/**
 * Element de l'arborescence du projet importe.
 * Pattern Composite : un repertoire et un fichier sont manipules uniformement.
 */
public sealed interface ProjectNode permits FileNode, DirectoryNode {

    String name();

    Path path();

    long sizeInBytes();

    <R> R accept(ProjectVisitor<R> visitor);
}
