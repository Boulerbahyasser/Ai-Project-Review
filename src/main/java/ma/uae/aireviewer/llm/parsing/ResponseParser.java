package ma.uae.aireviewer.llm.parsing;

import ma.uae.aireviewer.llm.LlmInvalidResponseException;
import ma.uae.aireviewer.llm.LlmResponse;

/** Transforme une reponse brute en structure exploitable, ou echoue explicitement. */
public interface ResponseParser {

    EvaluationPayload parse(LlmResponse response) throws LlmInvalidResponseException;
}
