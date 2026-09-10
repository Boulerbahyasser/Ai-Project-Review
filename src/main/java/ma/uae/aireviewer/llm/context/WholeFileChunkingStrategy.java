package ma.uae.aireviewer.llm.context;

import java.util.List;
import ma.uae.aireviewer.project.model.FileNode;

/** Un fragment par fichier, tant que le budget le permet. */
public final class WholeFileChunkingStrategy implements ChunkingStrategy {

    @Override
    public String name() {
        return "whole-file";
    }

    @Override
    public List<CodeChunk> chunk(List<FileNode> files, ContextBudget budget) {
        throw new UnsupportedOperationException("TODO : lire chaque fichier jusqu'a saturation du budget");
    }
}
