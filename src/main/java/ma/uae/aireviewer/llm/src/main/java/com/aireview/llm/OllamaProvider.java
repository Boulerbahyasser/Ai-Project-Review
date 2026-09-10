package com.aireview.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * PATTERN: Adapter
 *
 * <p>{@code OllamaProvider} adapts Ollama's local REST API
 * ({@code POST /api/generate}) to the {@link LLMProvider} Strategy interface.
 * The rest of the system interacts solely with {@link LLMProvider#call(LLMRequest)} — the HTTP
 * request construction, JSON serialisation/deserialisation, and timeout handling are fully
 * encapsulated here.
 *
 * <p><b>Technology choice — Java {@code HttpClient} (JDK 11+):</b>
 * The standard library {@code java.net.http.HttpClient} is used deliberately over third-party
 * libraries (OkHttp, Apache HttpComponents). This eliminates an entire dependency class from the
 * project, reduces attack surface, and ensures compatibility with any JVM that satisfies the
 * Java 17 requirement already declared in the {@code pom.xml}.
 *
 * <p><b>Ollama API contract (as of Ollama ≥ 0.1.x):</b>
 * <pre>
 * POST http://localhost:11434/api/generate
 * Content-Type: application/json
 *
 * Request body:
 * {
 *   "model":  "gemma2:2b",
 *   "prompt": "...",
 *   "stream": false,
 *   "options": { "temperature": 0.1, "num_predict": 2048 }
 * }
 *
 * Response body (stream=false):
 * {
 *   "model":    "gemma2:2b",
 *   "response": "... model output ...",
 *   "done":     true
 * }
 * </pre>
 *
 * <p><b>Security note:</b> The prompt field is never logged. All HTTP-level errors are wrapped in
 * {@link LLMException} before propagating, preventing raw stack traces (which may contain request
 * details) from reaching the application boundary.
 *
 * @see LLMProvider
 * @see Config
 */
public class OllamaProvider implements LLMProvider {

    // -----------------------------------------------------------------------
    // Internal Jackson DTOs for the Ollama wire format
    // -----------------------------------------------------------------------

    /**
     * Serialisation DTO for the Ollama {@code /api/generate} request body.
     * Fields use {@code @JsonProperty} to match Ollama's exact JSON key names.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record OllamaRequestBody(
            @JsonProperty("model")   String model,
            @JsonProperty("prompt")  String prompt,
            @JsonProperty("stream")  boolean stream,
            @JsonProperty("format")  String format,
            @JsonProperty("options") Options options
    ) {
        record Options(
                @JsonProperty("temperature") double temperature,
                @JsonProperty("num_predict") int    numPredict
        ) {}
    }

    /**
     * Deserialisation DTO for the Ollama {@code /api/generate} response body.
     *
     * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} absorbs extra fields
     * (e.g., {@code "total_duration"}, {@code "eval_count"}) that Ollama appends to the
     * response without breaking the parser.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaResponseBody(
            @JsonProperty("model")    String  model,
            @JsonProperty("response") String  response,
            @JsonProperty("done")     boolean done
    ) {}

    // -----------------------------------------------------------------------
    // PATTERN: Builder — Config
    // -----------------------------------------------------------------------

    /**
     * PATTERN: Builder
     *
     * <p>Immutable configuration object for {@code OllamaProvider}, constructed via its
     * nested {@link Builder}. This avoids a telescoping-constructor anti-pattern for what
     * would otherwise be a 4-parameter constructor where several arguments have the same
     * type ({@code String}, {@code int}).
     *
     * <p>The Builder pattern also makes test setup and future extension trivial: adding a new
     * configuration option (e.g., TLS certificate path) requires only a new setter on the
     * Builder without breaking any existing call sites.
     */
    public static final class Config {

        /** Default Ollama base URL (local instance). */
        public static final String DEFAULT_BASE_URL   = "http://localhost:11434";

        /** Default model (lightweight, suitable for code evaluation). */
        public static final String DEFAULT_MODEL      = "gemma2:2b";

        /** Default connect + read timeout in seconds. */
        public static final int    DEFAULT_TIMEOUT_SEC = 120;

        /** Default format to enforce JSON grammar-constrained sampling in Ollama. */
        public static final String DEFAULT_FORMAT      = "json";

        private final String baseUrl;
        private final String defaultModel;
        private final int    timeoutSeconds;
        private final String format;

        private Config(Builder builder) {
            this.baseUrl        = builder.baseUrl;
            this.defaultModel   = builder.defaultModel;
            this.timeoutSeconds = builder.timeoutSeconds;
            this.format         = builder.format;
        }

        public String getBaseUrl()       { return baseUrl; }
        public String getDefaultModel()  { return defaultModel; }
        public int    getTimeoutSeconds(){ return timeoutSeconds; }
        public String getFormat()        { return format; }

        /** Returns a {@code Config} with all default values. */
        public static Config defaults() {
            return new Builder().build();
        }

        /** Entry point for the fluent Builder. */
        public static Builder builder() { return new Builder(); }

        // -----------------------------------------------------------------------
        // PATTERN: Builder (nested)
        // -----------------------------------------------------------------------

        /**
         * PATTERN: Builder
         *
         * <p>Fluent builder for {@link Config}. Each setter validates its input immediately
         * (fail-fast) so that {@link #build()} can guarantee a fully valid {@code Config}.
         */
        public static final class Builder {
            private String baseUrl        = DEFAULT_BASE_URL;
            private String defaultModel   = DEFAULT_MODEL;
            private int    timeoutSeconds = DEFAULT_TIMEOUT_SEC;
            private String format         = DEFAULT_FORMAT;

            private Builder() {}

            /**
             * Sets the Ollama base URL.
             *
             * @param baseUrl e.g. {@code "http://localhost:11434"}; must not be null or blank
             * @return this builder (fluent)
             */
            public Builder baseUrl(String baseUrl) {
                if (baseUrl == null || baseUrl.isBlank()) {
                    throw new IllegalArgumentException("Config.Builder: baseUrl must not be null or blank.");
                }
                this.baseUrl = baseUrl;
                return this;
            }

            /**
             * Sets the default model identifier.
             *
             * @param model e.g. {@code "llama3:8b"}; must not be null or blank
             * @return this builder (fluent)
             */
            public Builder defaultModel(String model) {
                if (model == null || model.isBlank()) {
                    throw new IllegalArgumentException("Config.Builder: model must not be null or blank.");
                }
                this.defaultModel = model;
                return this;
            }

            /**
             * Sets the HTTP connect + read timeout.
             *
             * @param seconds must be positive
             * @return this builder (fluent)
             */
            public Builder timeoutSeconds(int seconds) {
                if (seconds <= 0) {
                    throw new IllegalArgumentException(
                            "Config.Builder: timeoutSeconds must be positive, got: " + seconds);
                }
                this.timeoutSeconds = seconds;
                return this;
            }

            /**
             * Sets the output format constraint (e.g. {@code "json"} or {@code null}).
             *
             * @param format the format constraint, or null to disable
             * @return this builder (fluent)
             */
            public Builder format(String format) {
                this.format = format;
                return this;
            }

            /** Builds and returns the immutable {@link Config}. */
            public Config build() {
                return new Config(this);
            }
        }
    }

    // -----------------------------------------------------------------------
    // OllamaProvider fields
    // -----------------------------------------------------------------------

    private final Config     config;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Creates an {@code OllamaProvider} with the given configuration.
     *
     * @param config provider configuration; must not be null
     */
    public OllamaProvider(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("OllamaProvider: config must not be null.");
        }
        this.config = config;
        this.mapper = new ObjectMapper();
        // Build a shared HttpClient. Using a shared instance is thread-safe and avoids
        // the overhead of creating a new connection pool on every call.
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .build();
    }

    /**
     * Creates an {@code OllamaProvider} with default configuration.
     * Equivalent to {@code new OllamaProvider(Config.defaults())}.
     */
    public OllamaProvider() {
        this(Config.defaults());
    }

    // -----------------------------------------------------------------------
    // LLMProvider implementation (Adapter pattern)
    // -----------------------------------------------------------------------

    /**
     * PATTERN: Adapter — adapts Ollama's HTTP/JSON protocol to the {@link LLMProvider} contract.
     *
     * <p>Steps:
     * <ol>
     *   <li>Serialise the {@link LLMRequest} into an {@link OllamaRequestBody} JSON string.</li>
     *   <li>POST the JSON to {@code <baseUrl>/api/generate} with the configured timeout.</li>
     *   <li>Assert an HTTP 200 response; throw {@link LLMException} for any other status.</li>
     *   <li>Deserialise the response body into an {@link OllamaResponseBody}.</li>
     *   <li>Wrap the model's text output in an {@link LLMResponse} and return it.</li>
     * </ol>
     *
     * <p>All checked exceptions ({@code IOException}, {@code InterruptedException},
     * {@code JsonProcessingException}) are caught and re-wrapped as {@link LLMException} so
     * the caller is never exposed to transport-layer details.
     *
     * @param request the fully constructed request; never null
     * @return a non-null {@link LLMResponse} on success
     * @throws LLMException on any network, HTTP, or JSON parsing failure
     */
    @Override
    public LLMResponse call(LLMRequest request) throws LLMException {
        if (request == null) {
            throw new LLMException("OllamaProvider: request must not be null.");
        }

        long startTime = System.currentTimeMillis();

        // Step 1 — Build the Ollama request body.
        OllamaRequestBody body = new OllamaRequestBody(
                request.model(),
                request.prompt(),
                false, // stream=false: receive complete response at once
                config.getFormat(),
                new OllamaRequestBody.Options(request.temperature(), request.maxTokens())
        );

        // Step 2 — Serialise to JSON.
        String requestJson;
        try {
            requestJson = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new LLMException("OllamaProvider: failed to serialise request body.", e);
        }

        // Step 3 — Build and send the HTTP request.
        HttpRequest httpRequest;
        try {
            httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl() + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
        } catch (IllegalArgumentException e) {
            throw new LLMException(
                    "OllamaProvider: invalid URI constructed from baseUrl='"
                    + config.getBaseUrl() + "'.", e);
        }

        HttpResponse<String> httpResponse;
        try {
            httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new LLMException(
                    "OllamaProvider: I/O error during HTTP call to Ollama. "
                    + "Is Ollama running at " + config.getBaseUrl() + "?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupt flag
            throw new LLMException(
                    "OllamaProvider: HTTP call was interrupted.", e);
        }

        // Step 4 — Check HTTP status code.
        if (httpResponse.statusCode() != 200) {
            throw new LLMException(
                    "OllamaProvider: unexpected HTTP status " + httpResponse.statusCode()
                    + " from Ollama. Body: " + truncate(httpResponse.body(), 200));
        }

        // Step 5 — Deserialise the response body.
        OllamaResponseBody ollamaResponse;
        try {
            ollamaResponse = mapper.readValue(httpResponse.body(), OllamaResponseBody.class);
        } catch (JsonProcessingException e) {
            throw new LLMException(
                    "OllamaProvider: failed to parse Ollama response JSON. "
                    + "Raw body (first 200 chars): " + truncate(httpResponse.body(), 200), e);
        }

        if (ollamaResponse.response() == null) {
            throw new LLMException(
                    "OllamaProvider: Ollama response contained a null 'response' field.");
        }

        long duration = System.currentTimeMillis() - startTime;
        return new LLMResponse(ollamaResponse.response(), ollamaResponse.model(), duration);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Truncates a string to {@code maxLen} characters, appending "..." if truncated.
     * Used to prevent large response bodies from flooding exception messages.
     */
    private static String truncate(String s, int maxLen) {
        if (s == null)             return "<null>";
        if (s.length() <= maxLen)  return s;
        return s.substring(0, maxLen) + "...";
    }

    /** Returns the current configuration (for inspection and testing). */
    public Config getConfig() {
        return config;
    }
}
