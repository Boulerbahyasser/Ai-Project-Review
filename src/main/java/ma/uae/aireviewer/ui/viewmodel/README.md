# `ui.viewmodel` — Etat d'affichage

Etat pret a afficher, sans regle metier. Couche tampon entre les DTO de
`application` et les composants JavaFX.

## Contenu

| Classe | Role |
|---|---|
| `ProjectTreeItemViewModel` | Libelle, type, est-ce un repertoire |
| `AnalysisProgressViewModel` | Avancement, total, critere en cours, ratio |
| `CriterionResultViewModel` | Libelle, score, statut, forces, faiblesses, recommandations |

## Pourquoi une couche de plus

Les types de `analysis` portent des `CriterionId`, des `ResultStatus`, des poids,
des `Instant`. Une vue a besoin de chaines et de nombres prets a afficher :
un libelle lisible, un statut traduit, un ratio entre 0 et 1.

Sans cette couche, la conversion se ferait dans les vues — et la logique de
presentation se disperserait dans le code JavaFX.

## Ce qui est autorise ici

Du calcul de **presentation** uniquement, comme `ratio()` dans
`AnalysisProgressViewModel`. Pas de calcul metier : un score consolide se calcule
dans `analysis.aggregation`, jamais ici.

## Regle

Des `record` immuables. Un nouvel etat d'affichage produit un nouvel objet, ce qui
evite les incoherences quand plusieurs evenements arrivent rapidement.
