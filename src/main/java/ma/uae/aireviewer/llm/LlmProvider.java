package ma.uae.aireviewer.llm;

/**
 * Abstraction d'acces aux modeles de langage (cahier des charges, section 4.1).
 * Unique porte d'entree : aucun appel HTTP vers un LLM ne doit exister ailleurs.
 * Remplacer un fournisseur = fournir une autre implementation, sans modifier analysis.
 */
public interface LlmProvider {

    String id();

    LlmResponse ask(LlmRequest request) throws LlmException;
}
