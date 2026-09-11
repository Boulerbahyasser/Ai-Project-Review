package ma.uae.aireviewer.project.model;

/** Visiteur de l'arborescence du projet (parcours decouple de la structure). */
public interface ProjectVisitor<R> {

    R visitFile(FileNode file);

    R visitDirectory(DirectoryNode directory);
}
