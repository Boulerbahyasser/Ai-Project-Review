package ma.uae.aireviewer.llm.resilience;

import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.LlmResponse;

/**
 * Strategie de repli : bascule sur un fournisseur secondaire quand le principal
 * reste en echec (cahier des charges, section 5). Repond aussi a l'exigence de
 * non-dependance a un modele unique.
 */
public final class FallbackLlmProvider implements LlmProvider {

    private final LlmProvider primary;
    private final LlmProvider secondary;

    public FallbackLlmProvider(LlmProvider primary, LlmProvider secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public String id() {
        return primary.id() + "+fallback:" + secondary.id();
    }

    @Override
    public LlmResponse ask(LlmRequest request) throws LlmException {
        try {
            return primary.ask(request);
        } catch (LlmException primaryFailure) {
            return secondary.ask(request);
        }
    }
}
