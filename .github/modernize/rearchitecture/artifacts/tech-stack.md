# Existing technology stack

- Java 21 source/target and Maven jar packaging (`pom.xml`).
- JavaFX 21.0.4 controls/FXML with `javafx-maven-plugin` 0.0.8.
- Jackson databind 2.17.1 in the LLM subtree.
- JUnit Jupiter 5.10.3 and Surefire 3.2.5.
- SnakeYAML is referenced by `YamlConfigurationLoader` but no SnakeYAML dependency is declared in `pom.xml`.
- Runtime resources include FXML, CSS, YAML profiles, prompt text, Logback configuration, and a LaTeX template.
- Dockerfiles exist for sandbox and LaTeX compilation, but the non-LLM Docker compiler/sandbox implementations are still stubs.

## Build/runtime evidence

`mvn clean test` reaches compilation and runs 260 tests in the selected source/test roots, with 1 failing test in the frozen LLM subtree. The configured compiler includes do not cover the application skeleton.

## Non-LLM blockers

There are 27 `UnsupportedOperationException("TODO...")` sites outside `llm`, including project loaders, analysis orchestration, report rendering/compilation, persistence, configuration consumers, security, and application use cases.
