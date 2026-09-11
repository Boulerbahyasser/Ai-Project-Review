package ma.uae.aireviewer.project.selection;

import java.util.EnumSet;
import java.util.Set;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.FileType;

/** Ne retient que certains types de fichiers (pertinents pour le critere evalue). */
public final class FileTypeSelectionStrategy implements FileSelectionStrategy {

    private final Set<FileType> accepted;

    public FileTypeSelectionStrategy(Set<FileType> accepted) {
        this.accepted = accepted == null || accepted.isEmpty()
                ? EnumSet.noneOf(FileType.class) : EnumSet.copyOf(accepted);
    }

    @Override
    public String name() {
        return "file-type";
    }

    @Override
    public boolean accepts(FileNode file) {
        return accepted.contains(file.type());
    }
}
