package ma.uae.aireviewer.llm.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmInvalidResponseException;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.LlmResponse;
import ma.uae.aireviewer.llm.LlmTimeoutException;
import ma.uae.aireviewer.llm.LlmUnavailableException;
import ma.uae.aireviewer.llm.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport HTTP mutualise (pattern Template Method).
 *
 * <p>{@link #ask(LlmRequest)} est {@code final} : la sequence d'appel, la mesure de
 * latence et surtout la traduction des erreurs sont ecrites une seule fois. Chaque
 * fournisseur ne specialise que trois points : son URL, le format de la requete,
 * l'extraction de la reponse.
 *
 * <p>Aucun appel HTTP vers un modele ne doit exister ailleurs dans l'application
 * (cahier des charges, section 16.2).
 */
public abstract class AbstractHttpLlmProvider implements LlmProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpLlmProvider.class);

    /** Longueur maximale d'un corps d'erreur recopie dans un message d'exception. */
    private static final int ERROR_EXCERPT_LENGTH = 300;

    protected final LlmConfig config;
    protected final ObjectMapper json = new ObjectMapper();

    private final HttpClient httpClient;
    private final String apiKey;

    protected AbstractHttpLlmProvider(LlmConfig config, String apiKey) {
        this.config = config;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(1, config.timeoutSeconds())))
                .build();
    }

    @Override
    public final LlmResponse ask(LlmRequest request) throws LlmException {
        HttpRequest httpRequest = buildHttpRequest(request);

        long startedAt = System.nanoTime();
        HttpResponse<String> httpResponse = send(httpRequest);
        Duration latency = Duration.ofNanos(System.nanoTime() - startedAt);

        verifyStatus(httpResponse);

        JsonNode body = parse(httpResponse.body());
        String content = extractContent(body);
        if (content == null || content.isBlank()) {
            throw new LlmInvalidResponseException(
                    "Le modele a renvoye un contenu vide (fournisseur " + id() + ")");
        }

        TokenUsage usage = extractUsage(body);
        LOG.debug("Appel {} termine en {} ms, {} jetons",
                id(), latency.toMillis(), usage.total());

        return new LlmResponse(
                content,
                servedModel(body, request.model()),
                usage.promptTokens(),
                usage.completionTokens(),
                latency);
    }

    // ---------------------------------------------------------------- sequence

    private HttpRequest buildHttpRequest(LlmRequest request) throws LlmException {
        String payload = serialize(requestBody(request));

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint()))
                // Le delai doit porter sur la requete entiere : un delai de connexion
                // seul ne protege pas d'une generation lente, cas frequent avec un
                // modele local.
                .timeout(Duration.ofSeconds(Math.max(1, config.timeoutSeconds())))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));

        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder.build();
    }

    private HttpResponse<String> send(HttpRequest httpRequest) throws LlmException {
        try {
            return httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException timeout) {
            throw new LlmTimeoutException(
                    "Pas de reponse de " + id() + " en " + config.timeoutSeconds() + " s", timeout);
        } catch (IOException unreachable) {
            throw new LlmUnavailableException(
                    "Service " + id() + " injoignable sur " + endpoint()
                            + " — le serveur est-il demarre ?", unreachable);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new LlmException("Appel a " + id() + " interrompu", interrupted);
        }
    }

    /**
     * Traduit le code HTTP en exception du domaine. La distinction entre erreur
     * transitoire et definitive se fait ici, et nulle part ailleurs : c'est elle
     * qui pilote la politique de reprise (section 5).
     */
    private void verifyStatus(HttpResponse<String> response) throws LlmException {
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return;
        }
        String excerpt = excerpt(response.body());

        switch (status) {
            case 401, 403 -> throw new LlmException(
                    "Authentification refusee par " + id() + " (HTTP " + status
                            + "). Verifiez la variable d'environnement "
                            + config.apiKeyEnvVar() + ". " + excerpt);
            case 404 -> throw new LlmException(
                    "Point d'entree introuvable (HTTP 404) : " + endpoint()
                            + ". Verifiez baseUrl dans config/application.yaml. " + excerpt);
            case 408, 504 -> throw new LlmTimeoutException(
                    "Delai depasse cote serveur " + id() + " (HTTP " + status + "). " + excerpt);
            case 429 -> throw new LlmUnavailableException(
                    "Debit ou quota depasse chez " + id() + " (HTTP 429). " + excerpt);
            default -> {
                if (status >= 500) {
                    throw new LlmUnavailableException(
                            "Service " + id() + " en erreur (HTTP " + status + "). " + excerpt);
                }
                throw new LlmException(
                        "Requete refusee par " + id() + " (HTTP " + status + "). " + excerpt);
            }
        }
    }

    private String serialize(ObjectNode body) throws LlmException {
        try {
            return json.writeValueAsString(body);
        } catch (JsonProcessingException malformed) {
            throw new LlmException("Corps de requete inserialisable pour " + id(), malformed);
        }
    }

    private JsonNode parse(String body) throws LlmInvalidResponseException {
        if (body == null || body.isBlank()) {
            throw new LlmInvalidResponseException("Reponse HTTP vide de " + id());
        }
        try {
            return json.readTree(body);
        } catch (JsonProcessingException malformed) {
            throw new LlmInvalidResponseException(
                    "Reponse de " + id() + " illisible : " + excerpt(body), malformed);
        }
    }

    private static String excerpt(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String flat = body.replaceAll("\s+", " ").trim();
        return flat.length() <= ERROR_EXCERPT_LENGTH
                ? flat
                : flat.substring(0, ERROR_EXCERPT_LENGTH) + "...";
    }

    // ------------------------------------------------- points de specialisation

    /** URL complete du point d'entree du fournisseur. */
    protected abstract String endpoint();

    /**
     * Traduit notre requete vers le format du fournisseur.
     *
     * <p>Retourne un arbre Jackson et non une chaine : l'echappement du code source
     * present dans les prompts est ainsi garanti par construction. Utilisez
     * {@link #json} pour creer le noeud racine.
     */
    protected abstract ObjectNode requestBody(LlmRequest request);

    /** Extrait le texte utile de la reponse du fournisseur. */
    protected abstract String extractContent(JsonNode response) throws LlmException;

    /** Jetons consommes. A redefinir si le fournisseur les communique. */
    protected TokenUsage extractUsage(JsonNode response) {
        return TokenUsage.UNKNOWN;
    }

    /** Modele reellement servi. A redefinir si le fournisseur le renvoie. */
    protected String servedModel(JsonNode response, String requested) {
        return requested;
    }
}
