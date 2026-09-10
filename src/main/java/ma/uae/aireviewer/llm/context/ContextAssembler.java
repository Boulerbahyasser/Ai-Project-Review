package ma.uae.aireviewer.llm.context;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.project.model.FileNode;

/** Prepare le contexte d'un critere : decoupage puis resume si le volume est excessif. */
public final class ContextAssembler {

    private final ChunkingStrategy chunkingStrategy;
    private final ContextBudget budget;
    private final IntermediateSummarizer summarizer;

    public ContextAssembler(ChunkingStrategy chunkingStrategy,
                            ContextBudget budget,
                            IntermediateSummarizer summarizer) {
        this.chunkingStrategy = chunkingStrategy;
        this.budget = budget;
        this.summarizer = summarizer;
    }

    public List<CodeChunk> assemble(Criterion criterion, List<FileNode> files) {
        throw new UnsupportedOperationException("TODO");
    }
}
