package ma.uae.aireviewer.llm.context;

import java.util.List;
import ma.uae.aireviewer.llm.LlmException;

/** Reduit plusieurs fragments en une synthese reinjectable (section 4.2). */
public interface IntermediateSummarizer {

    CodeChunk summarize(List<CodeChunk> chunks) throws LlmException;
}
