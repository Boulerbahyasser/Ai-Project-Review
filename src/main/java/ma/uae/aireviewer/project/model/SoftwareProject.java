package ma.uae.aireviewer.project.model;

import java.util.ArrayList;
import java.util.List;

/** Projet importe : metadonnees + arborescence. Donnee non fiable par principe. */
public record SoftwareProject(ProjectMetadata metadata, DirectoryNode root) {

    /** Aplatit l'arborescence en liste de fichiers. */
    public List<FileNode> allFiles() {
        List<FileNode> files = new ArrayList<>();
        collect(root, files);
        return List.copyOf(files);
    }

    private static void collect(ProjectNode node, List<FileNode> sink) {
        switch (node) {
            case FileNode file -> sink.add(file);
            case DirectoryNode directory -> directory.children().forEach(child -> collect(child, sink));
        }
    }
}
