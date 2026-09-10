# AI Project Reviewer - LLM Subsystem

This module handles the integration, prompt construction, and result parsing for the AI-Assisted Software Project Evaluator. It is designed to be highly modular, resilient to errors, and secure against prompt injection attacks.

**Core Responsibilities:**

- Interface with external and local LLMs (default: `gemma2:2b` via Ollama).
- Construct structured prompts targeting specific architectural and code quality criteria.
- Parse and validate strict JSON responses.
- Provide an isolated layer to sanitize untrusted source code.

**Prerequisites:**

- Java 17+
- Ollama running locally with `gemma2:2b` (`ollama run gemma2:2b`).
- Maven/Gradle (Jackson dependencies for JSON parsing).

**Integration:**
The module exposes the `LLMProvider` interface and the `ResilientEvaluator` service. Other subsystems (UI, Report) should depend solely on these abstractions to remain decoupled from the underlying HTTP and JSON parsing logic.
