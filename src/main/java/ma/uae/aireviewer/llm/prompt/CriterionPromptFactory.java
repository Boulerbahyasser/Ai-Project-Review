package ma.uae.aireviewer.llm.prompt;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.context.CodeChunk;

/**
 * Construit la requete d'evaluation d'un critere : un prompt par critere,
 * jamais un prompt generique reutilise tel quel.
 */
public interface CriterionPromptFactory {

    LlmRequest requestFor(Criterion criterion, List<CodeChunk> chunks);
}
