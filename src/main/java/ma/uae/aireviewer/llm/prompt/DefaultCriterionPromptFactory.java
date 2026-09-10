package ma.uae.aireviewer.llm.prompt;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.context.CodeChunk;

/** Implementation fondee sur les gabarits de ressources et le PromptBuilder. */
public final class DefaultCriterionPromptFactory implements CriterionPromptFactory {

    private final PromptTemplateRepository templates;
    private final PromptBuilder promptBuilder;
    private final LlmConfig config;

    public DefaultCriterionPromptFactory(PromptTemplateRepository templates,
                                         PromptBuilder promptBuilder,
                                         LlmConfig config) {
        this.templates = templates;
        this.promptBuilder = promptBuilder;
        this.config = config;
    }

    @Override
    public LlmRequest requestFor(Criterion criterion, List<CodeChunk> chunks) {
        throw new UnsupportedOperationException(
                "TODO : rendre system-evaluator + criterion-evaluation et produire un LlmRequest en jsonMode");
    }
}
