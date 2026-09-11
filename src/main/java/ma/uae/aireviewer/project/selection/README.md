# `project.selection` — Quels fichiers envoyer au modele

Repond a une exigence explicite de la section 3.1 :
*« tous les fichiers d'un projet n'ont pas necessairement besoin d'etre envoyes a
un LLM. Votre architecture doit permettre de definir des regles d'inclusion ou
d'exclusion. »*

## Contenu

| Classe | Role |
|---|---|
| `FileSelectionStrategy` | **Strategy** : `name()` + `accepts(fichier)` |
| `GlobSelectionStrategy` | Motifs d'inclusion puis d'exclusion |
| `MaxSizeSelectionStrategy` | Exclut les fichiers trop volumineux |
| `FileTypeSelectionStrategy` | Ne retient que certains types |
| `CompositeSelectionStrategy` | Un fichier passe s'il satisfait **toutes** les regles |
| `FileSelector` | Applique la strategie a l'ensemble du projet |

## Pourquoi ce filtrage est indispensable

Sans lui, on envoie `target/`, les `.jar`, les images, `node_modules` : le budget
de contexte est consomme par du bruit, les couts explosent, et la qualite de
l'evaluation baisse. Les motifs par defaut sont dans `config/application.yaml`.

## Le premier de trois niveaux de filtrage

| Niveau | Question | Qui |
|---|---|---|
| **1** | **Quels fichiers sont analysables ?** | **ce package** |
| 2 | Lesquels concernent *ce critere* ? | `analysis` (`relevantFiles()`) |
| 3 | Lesquels tiennent dans *cette requete* ? | `llm.context` (budget) |

Ne pas confondre les trois : c'est la source classique de logique dupliquee.

## Ajouter une strategie (section 10)

Une implementation de `FileSelectionStrategy`, composable avec les autres via
`CompositeSelectionStrategy`. Aucun fichier existant modifie.
