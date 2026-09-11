# Implementation Guide

This index is not the full contract. Do not implement from this file alone; follow the artifact paths below.

## Global artifacts

- `unit_graph.yaml` — entry points, exported signatures, dependencies, and shared references.
- `migration_boundary.yaml` — session scope; the LLM subtree is frozen and no implementation edits are authorized.
- `wire_contracts.yaml` — FXML/JVM/Maven contracts and observed build-boundary conflicts.
- `shared_modules.yaml` — UI adapter/controller coupling and advisory split candidates.
- `cross_unit_state.yaml` — controller state passed into asynchronous work and report generation.
- `seams.yaml` — inferred UI-to-LLM seam; its frozen-side rule is authoritative for this session.
- `project-structure.md`, `tech-stack.md`, `data-model.md` — global structure, existing stack, and data model.

## Unit: javafx-main-window

- External trigger: JavaFX window loaded from `/fxml/main-view.fxml`.
- Read `units/javafx-main-window/behavior.yaml` for UI branches, side effects, errors, and worker-thread behavior.
- Read `units/javafx-main-window/bindings.yaml` for FXML reflection and runtime configuration.
- Read `units/javafx-main-window/unit_decomposition.yaml` for advisory split candidates only (`commit: false`).
- Filter global rows by `unit: javafx-main-window`, `used_by_units`, or seam `cut_between`.
- Completion evidence: report artifacts read, preserve FXML handlers and task state transitions, list unresolved non-LLM stubs, and cite build/test evidence.

## Unit: launcher

- External trigger: JVM `main(String[])`.
- Read `units/launcher/behavior.yaml` for the unresolved `MainApp` reference.
- Read `units/launcher/bindings.yaml` for entry-point convention.
- Read `units/launcher/unit_decomposition.yaml` for advisory splits.
- Filter global rows by `unit: launcher`.
- Completion evidence: verify the launcher target class and include a compile command that actually includes `Launcher.java`.

