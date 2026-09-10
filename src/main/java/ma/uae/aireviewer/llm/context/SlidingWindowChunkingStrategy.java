package ma.uae.aireviewer.llm.context;

import java.util.List;
import ma.uae.aireviewer.project.model.FileNode;

/** Decoupage d'un fichier volumineux en fenetres de lignes avec recouvrement. */
public final class SlidingWindowChunkingStrategy implements ChunkingStrategy {

    private final int windowLines;
    private final int overlapLines;

    public SlidingWindowChunkingStrategy(int windowLines, int overlapLines) {
        this.windowLines = windowLines;
        this.overlapLines = overlapLines;
    }

    @Override
    public String name() {
        return "sliding-window";
    }

    @Override
    public List<CodeChunk> chunk(List<FileNode> files, ContextBudget budget) {
        throw new UnsupportedOperationException("TODO : fenetres de windowLines avec recouvrement overlapLines");
    }
}
