package ma.uae.aireviewer.llm.prompt;

import java.util.Map;

/**
 * Gabarit de prompt charge depuis src/main/resources/prompts.
 * Les prompts sont des ressources versionnees, pas des chaines dispersees dans le code.
 */
public record PromptTemplate(String name, String content) {

    public String render(Map<String, String> variables) {
        throw new UnsupportedOperationException("TODO : substituer les jetons {{cle}}");
    }
}
