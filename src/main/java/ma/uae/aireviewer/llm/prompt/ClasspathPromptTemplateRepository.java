package ma.uae.aireviewer.llm.prompt;

/** Charge les gabarits depuis le classpath (/prompts/*.txt). */
public final class ClasspathPromptTemplateRepository implements PromptTemplateRepository {

    @Override
    public PromptTemplate byName(String name) {
        throw new UnsupportedOperationException("TODO : lire /prompts/{name}.txt depuis le classpath");
    }
}
