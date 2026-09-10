package ma.uae.aireviewer.llm.context;

/**
 * Enveloppe de contexte autorisee par requete (cahier des charges, section 4.2) :
 * un projet peut depasser largement ce qu'un appel accepte.
 */
public record ContextBudget(int maxCharsPerRequest, int maxFilesPerRequest, int maxChunksPerCriterion) {
}
