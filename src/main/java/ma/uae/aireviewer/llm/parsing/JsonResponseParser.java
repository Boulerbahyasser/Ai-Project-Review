package ma.uae.aireviewer.llm.parsing;

import java.util.List;
import ma.uae.aireviewer.llm.LlmInvalidResponseException;
import ma.uae.aireviewer.llm.LlmResponse;

/**
 * Parseur JSON tolerant aux ecarts frequents des modeles (bloc markdown, texte
 * avant ou apres l'objet), puis validation stricte par les ResponseValidator.
 */
public final class JsonResponseParser implements ResponseParser {

    private final List<ResponseValidator> validators;

    public JsonResponseParser(List<ResponseValidator> validators) {
        this.validators = List.copyOf(validators);
    }

    @Override
    public EvaluationPayload parse(LlmResponse response) throws LlmInvalidResponseException {
        throw new UnsupportedOperationException(
                "TODO : isoler l'objet JSON, deserialiser via Jackson, appliquer chaque validateur");
    }
}
