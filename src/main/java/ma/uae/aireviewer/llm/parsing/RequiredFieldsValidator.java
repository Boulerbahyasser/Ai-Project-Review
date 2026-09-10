package ma.uae.aireviewer.llm.parsing;

import ma.uae.aireviewer.llm.LlmInvalidResponseException;

/** Refuse une reponse dont le critere annonce est absent ou different de celui demande. */
public final class RequiredFieldsValidator implements ResponseValidator {

    private final String expectedCriterionId;

    public RequiredFieldsValidator(String expectedCriterionId) {
        this.expectedCriterionId = expectedCriterionId;
    }

    @Override
    public void validate(EvaluationPayload payload) throws LlmInvalidResponseException {
        if (payload.criterion() == null || payload.criterion().isBlank()) {
            throw new LlmInvalidResponseException("Champ criterion absent");
        }
        if (!expectedCriterionId.equals(payload.criterion())) {
            throw new LlmInvalidResponseException(
                    "Critere inattendu : " + payload.criterion() + " (attendu " + expectedCriterionId + ")");
        }
    }
}
