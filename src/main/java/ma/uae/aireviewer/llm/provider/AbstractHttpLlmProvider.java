package ma.uae.aireviewer.llm.provider;

import java.net.http.HttpClient;
import java.time.Duration;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.LlmResponse;

/**
 * Pattern Template Method applique au transport HTTP : la sequence d'appel
 * (construction du corps -> envoi -> lecture du contenu -> mesure de latence)
 * est ecrite une seule fois ; chaque fournisseur ne specialise que le format
 * de la requete et l'extraction de la reponse.
 */
public abstract class AbstractHttpLlmProvider implements LlmProvider {

    protected final LlmConfig config;
    protected final HttpClient httpClient;
    private final String apiKey;

    protected AbstractHttpLlmProvider(LlmConfig config, String apiKey) {
        this.config = config;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeoutSeconds()))
                .build();
    }

    @Override
    public final LlmResponse ask(LlmRequest request) throws LlmException {
        throw new UnsupportedOperationException(
                "TODO : serialiser via requestBody(), POST sur endpoint(), traduire les erreurs "
                        + "HTTP en LlmTimeout/LlmUnavailable, puis extractContent()");
    }

    /** Cle d'API resolue depuis l'environnement ; vide pour un modele local. */
    protected final String apiKey() {
        return apiKey == null ? "" : apiKey;
    }

    protected abstract String endpoint();

    protected abstract String requestBody(LlmRequest request);

    protected abstract String extractContent(String responseBody) throws LlmException;
}
