package ma.uae.aireviewer.llm.parsing;

import ma.uae.aireviewer.llm.LlmInvalidResponseException;

/** Refuse un score hors bornes ou une note maximale incoherente. */
public final class ScoreRangeValidator implements ResponseValidator {

    @Override
    public void validate(EvaluationPayload payload) throws LlmInvalidResponseException {
        if (payload.maxScore() <= 0) {
            throw new LlmInvalidResponseException("maxScore invalide : " + payload.maxScore());
        }
        if (payload.score() < 0 || payload.score() > payload.maxScore()) {
            throw new LlmInvalidResponseException(
                    "Score hors bornes : " + payload.score() + "/" + payload.maxScore());
        }
    }
}
