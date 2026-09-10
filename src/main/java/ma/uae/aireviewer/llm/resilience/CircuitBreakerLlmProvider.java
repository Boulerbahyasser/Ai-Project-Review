package ma.uae.aireviewer.llm.resilience;

import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.LlmResponse;

/**
 * Coupe-circuit : au-dela d'un seuil d'echecs consecutifs, cesse d'appeler le
 * service pour ne pas prolonger une analyse condamnee (serveur local arrete).
 */
public final class CircuitBreakerLlmProvider implements LlmProvider {

    private final LlmProvider delegate;
    private final int failureThreshold;

    public CircuitBreakerLlmProvider(LlmProvider delegate, int failureThreshold) {
        this.delegate = delegate;
        this.failureThreshold = failureThreshold;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public LlmResponse ask(LlmRequest request) throws LlmException {
        throw new UnsupportedOperationException("TODO : compter les echecs consecutifs et ouvrir le circuit");
    }
}
