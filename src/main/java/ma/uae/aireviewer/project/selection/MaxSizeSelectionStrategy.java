package ma.uae.aireviewer.project.selection;

import ma.uae.aireviewer.project.model.FileNode;

/** Exclut les fichiers trop volumineux pour un envoi au modele. */
public final class MaxSizeSelectionStrategy implements FileSelectionStrategy {

    private final long maxSizeInBytes;

    public MaxSizeSelectionStrategy(long maxSizeInBytes) {
        this.maxSizeInBytes = maxSizeInBytes;
    }

    @Override
    public String name() {
        return "max-size";
    }

    @Override
    public boolean accepts(FileNode file) {
        return file.sizeInBytes() <= maxSizeInBytes;
    }
}
