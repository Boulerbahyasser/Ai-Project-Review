package ma.uae.aireviewer.llm.resilience;

import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.LlmResponse;

/**
 * Pattern Decorator : rejoue les echecs transitoires (timeout, indisponibilite,
 * reponse non conforme) selon la RetryPolicy, sans que l'appelant le sache.
 */
public final class RetryingLlmProvider implements LlmProvider {

    private final LlmProvider delegate;
    private final RetryPolicy policy;

    public RetryingLlmProvider(LlmProvider delegate, RetryPolicy policy) {
        this.delegate = delegate;
        this.policy = policy;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public LlmResponse ask(LlmRequest request) throws LlmException {
        throw new UnsupportedOperationException(
                "TODO : boucle de tentatives, attente policy.backoffFor(n), "
                        + "abandon sur erreur non rejouable");
    }
}
