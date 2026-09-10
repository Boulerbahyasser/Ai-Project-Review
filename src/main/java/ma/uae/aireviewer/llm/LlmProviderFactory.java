package ma.uae.aireviewer.llm;

import ma.uae.aireviewer.configuration.LlmConfig;

/**
 * Pattern Factory : construit le transport d'un fournisseur donne.
 *
 * <p>Une implementation par fournisseur. Chacune porte ses propres exigences —
 * Mistral reclame une cle, un modele local n'en veut pas — ce qu'un {@code switch}
 * unique ne saurait exprimer sans se transformer en fourre-tout.
 *
 * <p>{@link #create(LlmConfig)} rend le <b>transport nu</b>. Les comportements
 * transverses (reprise, cache) sont ajoutes une seule fois par
 * {@link LlmProviderAssembler} : aucune fabrique ne peut se tromper d'ordre.
 *
 * <p>Ajouter un fournisseur (section 10) : une classe de transport, une fabrique,
 * une ligne dans la liste d'assemblage. Aucun fichier existant modifie.
 */
public interface LlmProviderFactory {

    boolean supports(String providerId);

    LlmProvider create(LlmConfig config);
}
