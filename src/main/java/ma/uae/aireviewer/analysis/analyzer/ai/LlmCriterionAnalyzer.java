package ma.uae.aireviewer.analysis.analyzer.ai;

import java.util.List;
import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.AbstractAnalyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.context.ContextAssembler;
import ma.uae.aireviewer.llm.parsing.ResponseParser;
import ma.uae.aireviewer.llm.prompt.CriterionPromptFactory;
import ma.uae.aireviewer.project.model.FileNode;

/**
 * Evaluation d'un critere par LLM. Seul point du sous-systeme analysis qui
 * dialogue avec un modele, et uniquement a travers l'interface LlmProvider :
 * changer de fournisseur n'impacte pas cette classe.
 */
public final class LlmCriterionAnalyzer extends AbstractAnalyzer {

    private final LlmProvider provider;
    private final CriterionPromptFactory promptFactory;
    private final ContextAssembler contextAssembler;
    private final ResponseParser responseParser;

    public LlmCriterionAnalyzer(Criterion criterion,
                                LlmProvider provider,
                                CriterionPromptFactory promptFactory,
                                ContextAssembler contextAssembler,
                                ResponseParser responseParser) {
        super(criterion);
        this.provider = provider;
        this.promptFactory = promptFactory;
        this.contextAssembler = contextAssembler;
        this.responseParser = responseParser;
    }

    @Override
    protected CriterionResult evaluate(AnalysisContext context, List<FileNode> files) {
        throw new UnsupportedOperationException(
                "TODO : assembler le contexte -> construire le prompt -> provider.ask "
                        + "-> parser et valider -> convertir en CriterionResult");
    }
}
