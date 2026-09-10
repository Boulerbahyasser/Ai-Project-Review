# `analysis.analyzer` — Comment on evalue

Le point d'extension principal du projet. Un analyseur evalue **un** critere.
Ajouter une methode d'analyse = ajouter une classe ici, sans toucher au moteur.

## Contenu

| Classe | Role |
|---|---|
| `Analyzer` | Contrat : `criterion()` + `analyze(context) -> CriterionResult` |
| `AbstractAnalyzer` | **Template Method** : fixe le deroulement, laisse 2 etapes specialisables |
| `AnalyzerFactory` | Contrat de construction d'un analyseur pour un critere donne |
| `AnalyzerRegistry` | Resout l'analyseur d'un critere via les fabriques enregistrees |

## Le Template Method, a comprendre avant d'ecrire un analyseur

`AbstractAnalyzer.analyze()` est **`final`** : le deroulement est verrouille.

```
analyze()  =  relevantFiles()  ->  si vide : SKIPPED  ->  evaluate()
```

Vous ne redefinissez que :

- `relevantFiles(context)` — optionnel, par defaut tous les fichiers selectionnes ;
- `evaluate(context, files)` — obligatoire, la logique propre au critere.

Consequence : le cas « aucun fichier concerne » est traite une seule fois pour
tous les analyseurs, presents et futurs.

## Les trois familles

| Sous-package | Quand |
|---|---|
| `deterministic/` | Le programme calcule seul, sans reseau |
| `ai/` | L'appreciation est confiee a un modele de langage |
| `decorator/` | Comportement transverse ajoute autour d'un analyseur |

## Ne va pas ici

- Un appel HTTP direct : passez par l'interface de `llm`.
- La regle de score global : elle est dans `analysis.aggregation`.

Reference : cahier des charges sections 3.3 et 10.
