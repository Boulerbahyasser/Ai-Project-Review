# `application.command` — Operations executables

Pattern **Command**, suggere par la section 6 pour *« representer les differentes
operations executables sur un projet »*.

## Contenu

| Classe | Role |
|---|---|
| `Command<R>` | Contrat : `name()` + `execute()` |
| `CommandExecutor` | Soumission en arriere-plan, retourne un `CompletableFuture` |

## Le probleme resolu

Une analyse dure plusieurs minutes. Si le clic sur « Lancer » appelle directement
le cas d'utilisation, la fenetre JavaFX gele jusqu'a la fin. Inacceptable, et
visible immediatement par le correcteur.

En transformant l'action en objet, on peut la soumettre a un executeur :

```java
executor.submit(new RunAnalysisCommand(request));   // rend la main aussitot
```

## Ce que la reification apporte en plus

| Capacite | Comment |
|---|---|
| Ne pas geler l'IHM | Execution sur un autre thread |
| Annuler | `CompletableFuture.cancel()` |
| Journaliser | `name()` identifie l'operation dans la trace |
| Mettre en file | Plusieurs analyses soumises a la suite |

## Attention a ne pas en faire un pattern decoratif

La section 6 penalise les patterns artificiels. Si vous appelez `execute()`
immediatement apres avoir cree la commande, elle ne sert a rien : autant appeler
le cas d'utilisation. Ce pattern se justifie **uniquement** parce que l'execution
est differee et hors du thread IHM.

## Regle

Une commande **delegue** a un cas d'utilisation, elle ne reimplemente rien.
