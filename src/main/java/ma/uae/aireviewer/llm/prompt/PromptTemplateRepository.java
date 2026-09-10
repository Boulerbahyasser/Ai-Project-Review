package ma.uae.aireviewer.llm.prompt;

/** Acces aux gabarits de prompts. */
public interface PromptTemplateRepository {

    PromptTemplate byName(String name);
}
