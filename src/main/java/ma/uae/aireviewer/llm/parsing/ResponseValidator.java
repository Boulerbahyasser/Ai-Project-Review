package ma.uae.aireviewer.llm.parsing;

import ma.uae.aireviewer.llm.LlmInvalidResponseException;

/**
 * Regle de validation d'une reponse (section 4.3 : valider avant utilisation).
 * Ajouter une regle = ajouter une implementation.
 */
public interface ResponseValidator {

    void validate(EvaluationPayload payload) throws LlmInvalidResponseException;
}
