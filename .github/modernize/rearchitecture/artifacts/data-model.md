# Existing data model

No ORM or database schema is present. Persistence is designed around JSON files and in-memory records.

## Entities/value records

- `SoftwareProject(metadata, root)` — imported project composite; `allFiles()` recursively flattens the tree.
- `ProjectMetadata` — source/project metadata.
- `DirectoryNode` and `FileNode` — composite project tree nodes.
- `AnalysisRecord(analysisId, projectName, date, score, maxScore, profileName, modelUsed, reportPath)` — history record intended for JSON persistence.
- `EvaluationReport(header, scoreTable, sections, summary)` — report representation.
- `AnalysisResult`, `CriterionResult`, and `OverallScore` — analysis outputs.

## Relationships and boundaries

`SoftwareProject` owns a `DirectoryNode` tree containing `FileNode` values. `AnalysisRecord` references report identity/path but no database foreign keys exist. `EvaluationReport` contains a score table and report sections. Transaction boundaries are not defined; persistence implementations are currently incomplete.
