package ma.uae.aireviewer.llm.cache;

import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.LlmResponse;
import ma.uae.aireviewer.persistence.cache.CacheStore;

/**
 * Evite les appels inutiles (cahier des charges, section 4.2) : une requete
 * identique n'est pas renvoyee au modele.
 */
public final class CachingLlmProvider implements LlmProvider {

    private final LlmProvider delegate;
    private final CacheStore cache;

    public CachingLlmProvider(LlmProvider delegate, CacheStore cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public LlmResponse ask(LlmRequest request) throws LlmException {
        throw new UnsupportedOperationException(
                "TODO : cle = empreinte (modele + prompts + parametres) ; lire puis alimenter le cache");
    }
}
