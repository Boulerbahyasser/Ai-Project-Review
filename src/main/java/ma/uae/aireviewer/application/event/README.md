# `application.event` — Progression et erreurs

Pattern **Observer**. Le moteur publie ce qui se passe ; l'IHM ecoute. Le moteur
ne connait pas l'IHM — sans quoi `analysis` dependrait de `ui` et la section 16.2
serait violee (*« une IHM contenant toute la logique metier »*).

## Contenu

| Classe | Role |
|---|---|
| `AnalysisEvent` | Interface scellee : `Started`, `CriterionStarted`, `CriterionCompleted`, `CriterionFailed`, `Warning`, `Completed`, `Failed` |
| `AnalysisListener` | Contrat d'abonne : `onEvent(event)` |
| `AnalysisEventPublisher` | Diffusion aux abonnes, liste concurrente |

## Pourquoi une interface scellee

Le compilateur connait les sept evenements possibles. Un `switch` sur un
`AnalysisEvent` est verifie : ajouter un huitieme evenement fera echouer la
compilation de tout code qui ne le traite pas. Aucun cas ne peut etre oublie
silencieusement.

## Ce que ces evenements alimentent (section 3.2)

L'IHM doit *« suivre la progression »* et *« afficher les erreurs rencontrees »*.
Ces deux exigences sont couvertes par `CriterionStarted` / `CriterionCompleted`
d'une part, `CriterionFailed` / `Warning` d'autre part.

## Piege du thread

Les evenements sont publies depuis le thread du moteur, pas celui de l'IHM.
Un abonne JavaFX doit rebasculer via `Platform.runLater` — c'est le role de
`ui.bridge.FxAnalysisListener`. Sans cela, l'IHM plante de facon aleatoire.

## Defaut de couplage connu

`DefaultAnalysisEngine` importe `AnalysisEventPublisher`, donc `analysis` depend
de `application` : une fleche qui remonte, contraire a la regle de
`docs/ARCHITECTURE.md`. Correction prevue : declarer le port d'ecoute dans
`analysis` et faire de ce publieur un adaptateur. A traiter, et a mentionner en
reponse a la question 9 de la section 19.
