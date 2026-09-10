# LLM Subsystem Architecture

## Subsystem Separation

Following the project specifications, the LLM integration is isolated within the `llm` package, interacting closely with `analysis`, `security`, and `report`[cite: 1].

## Applied Design Patterns

- **Adapter / Strategy:** The `LLMProvider` interface abstracts the LLM backend (e.g., `OllamaProvider`, `MockProvider`). This allows swapping models without modifying the analysis engine[cite: 1].
- **Strategy:** Evaluation criteria (Architecture, SOLID, Tests) are implemented as individual strategies, allowing new criteria to be added without modifying existing components[cite: 1].
- **Builder:** Used to construct complex, multi-part prompts (System Role, Examples, Untrusted Code) and to assemble the final LaTeX report[cite: 1].

## Security & Prompt Injection Mitigation

Source code is treated strictly as untrusted data[cite: 1]. The prompt builder wraps all evaluated code in explicit XML tags (e.g., `<untrusted_code>`) and strictly instructs the model to ignore any embedded directives within those bounds[cite: 1].

## Context Management

To handle projects exceeding the context window, the system uses:

1. **File Filtering:** Excludes non-essential files (.class, .jar).
2. **Chunking:** Splits large packages into intermediate analyses.
3. **Aggregation:** Summarizes intermediate JSON outputs before a final synthesis prompt.

## Resilience & Parsing

The evaluation engine handles model non-determinism by:

- Validating all responses against a strict `CriterionResult` JSON schema.
- Implementing a retry mechanism (exponential backoff) for HTTP errors or malformed JSON[cite: 1].
