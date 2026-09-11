# Diagramme de classes — sous-systeme LLM

Etabli a partir de `llm-subsystem/src/main/java/com/aireview/llm/`.

```mermaid
classDiagram
    direction TB

    class LLMProvider {
        <<interface>>
        +call(LLMRequest) LLMResponse
    }

    class OllamaProvider {
        -Config config
        -HttpClient httpClient
        -ObjectMapper mapper
        +OllamaProvider()
        +OllamaProvider(Config)
        +call(LLMRequest) LLMResponse
    }

    class MockLLMProvider {
        -int callCount
        -LLMRequest lastRequest
        -boolean failOnNextCall
        +MockLLMProvider()
        +MockLLMProvider(long)
        +call(LLMRequest) LLMResponse
        +getCallCount() int
        +getLastRequest() LLMRequest
        +setFailOnNextCall(boolean)
        +reset()
    }

    class Config {
        -String baseUrl
        -String defaultModel
        -int timeoutSeconds
        -String format
        +defaults() Config$
        +builder() Builder$
    }

    class Builder {
        +baseUrl(String) Builder
        +defaultModel(String) Builder
        +timeoutSeconds(int) Builder
        +format(String) Builder
        +build() Config
    }

    class ResilientEvaluator {
        +int DEFAULT_MAX_RETRIES$
        +long DEFAULT_BASE_DELAY_MS$
        -LLMProvider provider
        -int maxRetries
        -long baseDelayMs
        +ResilientEvaluator(LLMProvider)
        +ResilientEvaluator(LLMProvider, int, long)
        +evaluate(String, String, String) EvaluationResult
        -evaluateWithRetry(EvaluationCriterion, ...) CriterionResult
        -applyBackoff(int)
        ~extractJson(String, EvaluationCriterion, int) String$
    }

    class ChunkedEvaluator {
        -ResilientEvaluator resilientEvaluator
        -ContextSplitter splitter
        +ChunkedEvaluator(LLMProvider)
        +ChunkedEvaluator(ResilientEvaluator, ContextSplitter)
        +evaluate(String, String, String) EvaluationResult
    }

    class ContextSplitter {
        -int maxCharsPerChunk
        -int overlapLines
        +ContextSplitter()
        +ContextSplitter(int, int)
        +split(String) List~String~
        +shouldInclude(String) boolean$
        +filterFiles(List~String~) List~String~$
        +aggregateChunkResults(List~CriterionResult~) CriterionResult$
    }

    class PromptBuilder {
        -EvaluationCriterion criterion
        -String sourceCode
        -String projectName
        +forCriterion(EvaluationCriterion) PromptBuilder$
        +withSourceCode(String) PromptBuilder
        +withProjectName(String) PromptBuilder
        +build() String
        ~sanitiseSourceCode(String) String$
        -systemRole() String
        -fewShotExample() String
        -userPayload() String
    }

    class EvaluationCriterion {
        <<enumeration>>
        ARCHITECTURE
        SOLID_PRINCIPLES
        TESTING
        -String displayName
        -int maxScore
        -String evaluationGuidance
        +getDisplayName() String
        +getMaxScore() int
        +getEvaluationGuidance() String
    }

    class LLMRequest {
        <<record>>
        +String model
        +String prompt
        +double temperature
        +int maxTokens
        +withDefaults(String, String) LLMRequest$
    }

    class LLMResponse {
        <<record>>
        +String rawContent
        +String modelUsed
        +long durationMillis
        +looksLikeJson() boolean
    }

    class CriterionResult {
        <<record>>
        +String criterion
        +int score
        +int maxScore
        +String feedback
        +List~String~ issues
        +isValid() boolean
        +scorePercent() double
    }

    class EvaluationResult {
        <<record>>
        +String projectName
        +List~CriterionResult~ results
        +int totalScore
        +int maxTotalScore
        +long durationMillis
        +overallPercent() double
        +summary() String
    }

    class LLMException {
        +LLMException(String)
        +LLMException(String, Throwable)
    }

    LLMProvider <|.. OllamaProvider : implements
    LLMProvider <|.. MockLLMProvider : implements
    OllamaProvider *-- Config : nested
    Config +-- Builder : nested
    Builder ..> Config : creates

    ResilientEvaluator o--> LLMProvider : injecte
    ResilientEvaluator ..> PromptBuilder : utilise
    ResilientEvaluator ..> EvaluationCriterion : itere sur
    ResilientEvaluator ..> CriterionResult : produit
    ResilientEvaluator ..> EvaluationResult : produit

    ChunkedEvaluator o--> ResilientEvaluator
    ChunkedEvaluator o--> ContextSplitter
    ContextSplitter ..> CriterionResult : agrege

    PromptBuilder o--> EvaluationCriterion
    LLMProvider ..> LLMRequest : consomme
    LLMProvider ..> LLMResponse : produit
    LLMProvider ..> LLMException : leve
    EvaluationResult *-- CriterionResult : 1..*

    Exception <|-- LLMException
```

## Lecture du diagramme

**Le point de decouplage** est `LLMProvider`. `ResilientEvaluator` ne connait que
cette interface (agregation `o-->`), jamais `OllamaProvider`. C'est ce qui permet
de substituer `MockLLMProvider` en test, sans reseau (section 11).

**Deux niveaux de service** : `ResilientEvaluator` evalue un code qui tient dans
une requete ; `ChunkedEvaluator` l'enveloppe pour traiter les projets trop
volumineux, en deleguant le decoupage a `ContextSplitter`.

**Les donnees sont immuables** : six des huit types porteurs de donnees sont des
`record`. Aucun etat partage, donc aucun probleme de concurrence sur les
resultats.

## Deux observations a porter au rapport

**`EvaluationCriterion` est un enum.** Les criteres sont donc figes a la
compilation. La section 3.3 exige en gras qu'un critere puisse etre ajoute *sans
modifier les composants existants* — un enum impose de le modifier, et le sujet
en enumere dix-sept alors que trois sont presents.

**`ResilientEvaluator` n'est pas un Decorator.** Il recoit un `LLMProvider` mais
n'en implemente pas l'interface : c'est une couche de service. Consequence
visible sur le diagramme : la resilience ne peut pas etre empilee, ni desactivee,
ni testee independamment du reste de l'evaluation.
