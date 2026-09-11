# Project structure

## Project type

JavaFX desktop application with an embedded LLM integration subsystem. It is not a backend, web frontend, library, or monorepo at runtime. Maven currently packages a jar named `llm-subsystem`, although the repository also contains the broader reviewer skeleton.

## Entry points

- `src/main/java/ma/uae/aireviewer/ui/AiReviewerApplication.java:29` starts JavaFX and loads `/fxml/main-view.fxml`.
- `src/main/java/ma/uae/aireviewer/Launcher.java:10` is intended as a JVM launcher but references `ma.uae.aireviewer.ui.MainApp`, which is absent.
- `src/main/resources/fxml/main-view.fxml:7` dynamically binds `MainController`.

## Layers and counts

| Layer/package | Java files | Observed role |
|---|---:|---|
| `ui` | 9 | JavaFX view/controller/adapter/presentation models |
| `application` | 15 | facade, use cases, DTOs, events |
| `project` | 24 | loaders, classification, composite project model, selection |
| `analysis` | 33 | criteria, analyzers, decorators, aggregation, results |
| `report` | 19 | report model, assembly, rendering, LaTeX/PDF compilation |
| `persistence` | 5 | history and cache contracts/implementations |
| `configuration` | 11 | YAML/configuration records and secret provider |
| `security` | 14 | sandbox, prompt guard, secret redaction, policies |
| `llm` | 23 | separate provider/evaluator/report implementation and tests; frozen for this session |

The repository defines a layered architecture in `docs/ARCHITECTURE.md`, but the executable UI path bypasses `application`, `analysis`, `project`, `report`, and `persistence` and calls the LLM adapter directly.

## Key conflict

`pom.xml:77-86` restricts compilation to `com/aireview/llm/**` and `ma/uae/aireviewer/ui/**`. This masks the unfinished non-LLM packages and allows the stale `Launcher` reference to remain undetected.
