package com.aireview.llm;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Task 3: Local LLM Integration.
 *
 * <p><b>Testing strategy:</b> {@code OllamaProvider} requires a live Ollama process, which is
 * deliberately absent in CI. Therefore this suite is split into two concerns:
 *
 * <ol>
 *   <li><b>Unit tests (hermetic):</b> Verify the {@link OllamaProvider.Config} Builder, the
 *       {@code OllamaProvider} constructor guards, and the contract that {@code OllamaProvider}
 *       satisfies the {@link LLMProvider} interface at the type level. These tests run without any
 *       network access.</li>
 *   <li><b>Integration test placeholder:</b> A single method tagged {@code @Disabled} documents
 *       the expected end-to-end flow with a live Ollama instance, so the test can be enabled
 *       manually during local development (or in a Docker-based CI stage).</li>
 * </ol>
 *
 * <p>All contract-level assertions (correct JSON parsing, round-trip validation) use
 * {@link MockLLMProvider} as the concrete {@link LLMProvider}, consistent with the project rule
 * that every task's tests must utilize {@code MockLLMProvider}.
 */
@DisplayName("Task 3 – Local LLM Integration (OllamaProvider)")
class OllamaProviderTest {

    // =========================================================================
    // Section 1: Config Builder tests (no network required)
    // =========================================================================

    @Nested
    @DisplayName("1. Config Builder (PATTERN: Builder)")
    class ConfigBuilderTests {

        @Test
        @DisplayName("Config.defaults() produces expected default values")
        void defaultConfigHasExpectedValues() {
            OllamaProvider.Config cfg = OllamaProvider.Config.defaults();

            assertEquals("http://localhost:11434",  cfg.getBaseUrl());
            assertEquals("gemma2:2b",              cfg.getDefaultModel());
            assertEquals(120,                      cfg.getTimeoutSeconds());
        }

        @Test
        @DisplayName("Builder overrides baseUrl correctly")
        void builderOverridesBaseUrl() {
            OllamaProvider.Config cfg = OllamaProvider.Config.builder()
                    .baseUrl("http://192.168.1.100:11434")
                    .build();

            assertEquals("http://192.168.1.100:11434", cfg.getBaseUrl());
        }

        @Test
        @DisplayName("Builder overrides defaultModel correctly")
        void builderOverridesDefaultModel() {
            OllamaProvider.Config cfg = OllamaProvider.Config.builder()
                    .defaultModel("llama3:8b")
                    .build();

            assertEquals("llama3:8b", cfg.getDefaultModel());
        }

        @Test
        @DisplayName("Builder overrides timeoutSeconds correctly")
        void builderOverridesTimeout() {
            OllamaProvider.Config cfg = OllamaProvider.Config.builder()
                    .timeoutSeconds(30)
                    .build();

            assertEquals(30, cfg.getTimeoutSeconds());
        }

        @Test
        @DisplayName("Builder supports full fluent chaining")
        void builderFluentChainingWorks() {
            OllamaProvider.Config cfg = OllamaProvider.Config.builder()
                    .baseUrl("http://myhost:11434")
                    .defaultModel("mistral:7b")
                    .timeoutSeconds(60)
                    .build();

            assertEquals("http://myhost:11434", cfg.getBaseUrl());
            assertEquals("mistral:7b",          cfg.getDefaultModel());
            assertEquals(60,                    cfg.getTimeoutSeconds());
        }

        @Test
        @DisplayName("Builder rejects null baseUrl")
        void builderRejectsNullBaseUrl() {
            assertThrows(IllegalArgumentException.class,
                    () -> OllamaProvider.Config.builder().baseUrl(null));
        }

        @Test
        @DisplayName("Builder rejects blank baseUrl")
        void builderRejectsBlankBaseUrl() {
            assertThrows(IllegalArgumentException.class,
                    () -> OllamaProvider.Config.builder().baseUrl("   "));
        }

        @Test
        @DisplayName("Builder rejects null defaultModel")
        void builderRejectsNullModel() {
            assertThrows(IllegalArgumentException.class,
                    () -> OllamaProvider.Config.builder().defaultModel(null));
        }

        @Test
        @DisplayName("Builder rejects zero timeoutSeconds")
        void builderRejectsZeroTimeout() {
            assertThrows(IllegalArgumentException.class,
                    () -> OllamaProvider.Config.builder().timeoutSeconds(0));
        }

        @Test
        @DisplayName("Builder rejects negative timeoutSeconds")
        void builderRejectsNegativeTimeout() {
            assertThrows(IllegalArgumentException.class,
                    () -> OllamaProvider.Config.builder().timeoutSeconds(-5));
        }
    }

    // =========================================================================
    // Section 2: OllamaProvider construction (no network required)
    // =========================================================================

    @Nested
    @DisplayName("2. OllamaProvider Construction")
    class OllamaProviderConstructionTests {

        @Test
        @DisplayName("default constructor creates provider with default config")
        void defaultConstructorUsesDefaultConfig() {
            OllamaProvider provider = new OllamaProvider();
            assertNotNull(provider.getConfig());
            assertEquals("http://localhost:11434", provider.getConfig().getBaseUrl());
        }

        @Test
        @DisplayName("config constructor stores the provided config")
        void configConstructorStoresConfig() {
            OllamaProvider.Config cfg = OllamaProvider.Config.builder()
                    .baseUrl("http://test-host:11434")
                    .timeoutSeconds(45)
                    .build();
            OllamaProvider provider = new OllamaProvider(cfg);
            assertSame(cfg, provider.getConfig());
        }

        @Test
        @DisplayName("null config throws IllegalArgumentException")
        void nullConfigThrows() {
            assertThrows(IllegalArgumentException.class, () -> new OllamaProvider(null));
        }

        @Test
        @DisplayName("OllamaProvider satisfies LLMProvider type contract")
        void ollamaProviderImplementsLLMProvider() {
            // PATTERN: Strategy — verified at compile time by this assignment.
            LLMProvider provider = new OllamaProvider();
            assertNotNull(provider, "OllamaProvider must implement LLMProvider.");
        }
    }

    // =========================================================================
    // Section 3: Contract parity tests (MockLLMProvider vs OllamaProvider interface)
    // =========================================================================

    @Nested
    @DisplayName("3. Contract Parity (MockLLMProvider as Strategy reference)")
    class ContractParityTests {

        /**
         * Verifies that any class implementing {@link LLMProvider} produces a non-null,
         * well-formed {@link LLMResponse} when given a valid {@link LLMRequest}.
         *
         * <p>This helper method is the test that will also exercise {@code OllamaProvider}
         * when the integration test is enabled. Here we run it with the mock to confirm
         * the contract holds at the interface level.
         *
         * <p>PATTERN: Strategy — {@code provider} is typed as the interface.
         */
        private static void assertProviderContractHolds(LLMProvider provider) throws LLMException {
            LLMRequest req = LLMRequest.withDefaults("gemma2:2b",
                    "Evaluate the architecture of this project.");
            LLMResponse res = provider.call(req);

            assertNotNull(res,                  "Response must not be null.");
            assertNotNull(res.rawContent(),     "rawContent must not be null.");
            assertNotNull(res.modelUsed(),      "modelUsed must not be null.");
            assertFalse(res.modelUsed().isBlank(), "modelUsed must not be blank.");
            assertTrue(res.durationMillis() >= 0,  "durationMillis must be non-negative.");
        }

        @Test
        @DisplayName("MockLLMProvider satisfies the LLMProvider response contract")
        void mockSatisfiesContract() throws LLMException {
            assertProviderContractHolds(new MockLLMProvider());
        }

        @Test
        @DisplayName("MockLLMProvider response for 'architecture' is parseable JSON")
        void mockArchitectureResponseIsJson() throws LLMException {
            LLMProvider provider = new MockLLMProvider();
            LLMResponse res = provider.call(
                    LLMRequest.withDefaults("gemma2:2b", "Evaluate the architecture."));

            assertTrue(res.looksLikeJson(),
                    "Architecture response must look like JSON — same expectation for OllamaProvider.");
        }

        @Test
        @DisplayName("MockLLMProvider response for 'solid' is parseable JSON")
        void mockSolidResponseIsJson() throws LLMException {
            LLMProvider provider = new MockLLMProvider();
            LLMResponse res = provider.call(
                    LLMRequest.withDefaults("gemma2:2b", "Review solid principles."));
            assertTrue(res.looksLikeJson());
        }

        @Test
        @DisplayName("LLMException is thrown and propagated correctly by MockLLMProvider")
        void exceptionPropagationWorks() {
            MockLLMProvider mock = new MockLLMProvider();
            mock.setFailOnNextCall(true);

            LLMException ex = assertThrows(LLMException.class,
                    () -> mock.call(LLMRequest.withDefaults("gemma2:2b", "Evaluate.")));
            assertNotNull(ex.getMessage());
        }
    }

    // =========================================================================
    // Section 4: Integration test placeholder (disabled — requires live Ollama)
    // =========================================================================

    @Nested
    @DisplayName("4. Integration Test (Disabled — requires live Ollama at localhost:11434)")
    class IntegrationTests {

        /**
         * End-to-end test against a live Ollama instance.
         *
         * <p>To enable: remove {@code @Disabled} and ensure {@code gemma2:2b} is available:
         * <pre>
         *   ollama run gemma2:2b
         * </pre>
         *
         * <p>This test validates the complete Adapter chain:
         * {@code LLMRequest → OllamaProvider.call() → HTTP POST → JSON parse → LLMResponse}
         */
        @Test
        @Disabled("Requires live Ollama process. Run manually: ollama run gemma2:2b")
        @DisplayName("[INTEGRATION] OllamaProvider returns valid JSON for 'architecture' prompt")
        void ollamaProviderArchitectureIntegration() throws LLMException {
            OllamaProvider provider = new OllamaProvider(
                    OllamaProvider.Config.builder()
                            .timeoutSeconds(60)
                            .build()
            );

            LLMRequest req = LLMRequest.withDefaults(
                    OllamaProvider.Config.DEFAULT_MODEL,
                    """
                    You are a software architecture expert. Evaluate the following criterion.
                    Respond ONLY with valid JSON matching this schema:
                    {"criterion":"...","score":N,"maxScore":10,"feedback":"...","issues":["..."]}

                    Criterion: Architecture
                    <untrusted_code>
                    public class UserService {
                        private final UserRepository repo;
                        public UserService(UserRepository repo) { this.repo = repo; }
                    }
                    </untrusted_code>
                    """
            );

            LLMResponse res = provider.call(req);

            assertNotNull(res);
            assertTrue(res.looksLikeJson(),
                    "Live OllamaProvider must return JSON. Got: " + res.rawContent());
            assertTrue(res.durationMillis() > 0,
                    "Duration must be positive for a real network call.");
        }
    }
}
