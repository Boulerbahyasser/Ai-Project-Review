package ma.uae.aireviewer.llm.context;

import java.util.List;
import ma.uae.aireviewer.project.model.FileNode;

/**
 * Pattern Strategy : maniere de decouper le code pour tenir dans le budget.
 * Ajouter une strategie de decoupage = ajouter une implementation.
 */
public interface ChunkingStrategy {

    String name();

    List<CodeChunk> chunk(List<FileNode> files, ContextBudget budget);
}
