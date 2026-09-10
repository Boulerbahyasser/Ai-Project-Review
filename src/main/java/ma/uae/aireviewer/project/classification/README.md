# `project.classification` — Identification du type de fichier

La section 3.1 exige que l'application *« identifie les differents types de
fichiers »* : Java, configuration, Maven ou Gradle, Docker, documentation, tests,
scripts, ressources.

## Contenu

| Classe | Role |
|---|---|
| `FileClassifier` | Contrat : `classify(chemin) -> FileType` |
| `ClassificationRule` | Une regle unitaire : `matches(chemin)` + `type()` |
| `RuleBasedFileClassifier` | Applique une liste ordonnee, premiere regle satisfaite |

## Pourquoi une liste de regles plutot qu'un gros `switch`

Ajouter un type reconnu = ajouter une regle a la liste. Aucun fichier existant
modifie. L'ordre compte : `src/test/java/Foo.java` doit etre classe `TEST`, pas
`JAVA` — la regle des tests passe donc **avant** celle des sources.

## A quoi sert le type ensuite

| Consommateur | Usage |
|---|---|
| `project.selection` | Ne retenir que certains types |
| `analysis` | `TestPresenceAnalyzer` compte les `TEST` ; `DockerQualityAnalyzer` ne lit que les `DOCKER` |
| `ui` | Icone par type dans l'arbre |

## Piege a eviter

Ne pas classer uniquement sur l'extension. `Dockerfile` n'a pas d'extension,
`build.gradle.kts` en a deux, et un `.java` sous `src/test` n'est pas une source.
Les regles doivent pouvoir examiner **le chemin complet**, pas seulement le suffixe.
