# `ui.controller` — Reaction aux actions utilisateur

## Contenu

| Classe | Role |
|---|---|
| `MainController` | Traduit les actions en appels de la facade |

## Une seule dependance autorisee

```java
public final class MainController {
    private final AiReviewerFacade facade;      // et rien d'autre
}
```

C'est la raison d'etre du pattern Facade : sans elle, ce controleur declarerait
neuf dependances (chargeur, catalogue, selecteur, moteur, fournisseur, assembleur,
rendu, compilateur, historique) — et deviendrait exactement le defaut sanctionne
en section 16.2.

## Ce qu'une methode de controleur contient

Trois lignes typiques : lire la saisie, construire un DTO, appeler la facade.

```java
public void onStartAnalysis() {
    var requete = new RunAnalysisRequest(projetCourant, profilChoisi, fournisseur);
    executor.submit(new RunAnalysisCommand(facade, requete));   // hors du thread IHM
}
```

Aucune boucle, aucun calcul, aucun `try/catch` metier : les erreurs remontent par
les evenements et sont affichees par `ErrorPanel`.

## Piege du thread

Ne jamais appeler `facade.runAnalysis(...)` directement depuis un gestionnaire de
clic : l'analyse dure des minutes et la fenetre gelerait. Passez par
`application.command.CommandExecutor`.
