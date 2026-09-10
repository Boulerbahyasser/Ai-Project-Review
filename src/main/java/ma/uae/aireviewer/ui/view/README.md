# `ui.view` — Composants d'affichage

Les huit exigences de la section 3.2 se repartissent sur ces classes.

## Contenu

| Classe | Exigence de la section 3.2 |
|---|---|
| `MainView` | Assemblage de la fenetre |
| `ProjectTreeView` | *« voir l'arborescence du projet »* |
| `AnalysisConfigView` | *« choisir une configuration d'analyse »* |
| `ProgressView` | *« suivre la progression »* |
| `ResultsView` | *« consulter les resultats »* |
| `ErrorPanel` | *« afficher les erreurs rencontrees »* |

Les deux exigences restantes — *« choisir un projet »* et *« generer un rapport »* —
sont des actions, portees par `ui.controller`.

## Interdits dans ce package

- Aucun appel HTTP, aucun `docker`, aucun `pdflatex`.
- Aucun calcul de score, aucun parcours de fichiers du projet analyse.
- Aucun `import ma.uae.aireviewer.llm.*` ni `...analysis.analyzer.*`.
- Aucune lecture de `config/application.yaml`.

Une vue **affiche** un view-model et **signale** une action au controleur.
Rien d'autre.

## Pourquoi c'est le package le plus surveille

La section 3.2 previent : *« la separation entre l'IHM et la logique metier sera
evaluee particulierement attentivement »*, et la section 16.2 cite
*« une IHM contenant toute la logique metier »* parmi les defauts sanctionnes.

Regle de relecture : si une classe de ce package importe autre chose que JavaFX,
`ui.viewmodel` et `application`, c'est probablement une erreur.

## Sur `ProjectTreeView`

L'arbre affiche vient de `project.model` (Composite). Convertissez les noeuds en
`ProjectTreeItemViewModel` avant l'affichage : la vue ne doit pas parcourir
elle-meme l'arborescence du projet analyse.
