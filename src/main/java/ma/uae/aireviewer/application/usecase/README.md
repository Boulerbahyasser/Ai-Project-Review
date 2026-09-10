# `application.usecase` — Les cas d'utilisation

Un cas d'utilisation = une action metier de la section 2 du cahier des charges.
Chacun coordonne plusieurs sous-systemes, sans contenir de regle metier lui-meme.

## Contenu

| Classe | Action |
|---|---|
| `ImportProjectUseCase` | Charger un projet et construire son arborescence |
| `RunAnalysisUseCase` | Lancer une analyse et enregistrer son resultat |
| `GenerateReportUseCase` | Produire le rapport a partir d'un resultat |
| `ViewHistoryUseCase` | Consulter l'historique |

## Ce qu'un cas d'utilisation fait, et ne fait pas

**Il coordonne** : charger le profil, construire le contexte, appeler le moteur,
enregistrer, publier des evenements.

**Il ne calcule pas** : aucun score, aucun parcours de fichiers, aucune
construction de prompt. S'il commence a calculer, la logique doit descendre dans
le sous-systeme concerne.

Test simple : un cas d'utilisation qui depasse une trentaine de lignes contient
probablement de la logique qui n'est pas a sa place.

## Ajouter un cas d'utilisation

Une classe ici + une methode sur la facade. Les extensions de la section 18
(comparer deux versions d'un projet, comparer plusieurs modeles, resume comparatif
de plusieurs projets) s'ajoutent exactement comme ca.

## Regle

Les dependances arrivent par le **constructeur**, sous forme d'interfaces. C'est
ce qui rend ces classes testables avec des doublures, sans reseau ni fichiers
(section 11).
