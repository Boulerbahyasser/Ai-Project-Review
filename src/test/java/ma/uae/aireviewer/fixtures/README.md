# `fixtures` — Doublures et donnees de test

Le contenu de ce repertoire conditionne le travail de toute l'equipe : il permet
de tester chaque sous-systeme sans reseau, sans Docker et sans IHM.

## A ecrire en priorite

| Classe | Role | Debloque |
|---|---|---|
| `FakeLlmProvider` | Renvoie des reponses preparees ou leve des exceptions choisies | `analysis`, `llm.parsing`, `application` |
| `ProjectFixtures` | Construit des `SoftwareProject` en memoire, sans disque | `analysis`, `project.selection` |
| `AnalysisResultFixtures` | Resultats d'analyse fabriques a la main | `report`, `persistence` |
| `InMemoryCacheStore` | Cache en memoire | `llm.cache`, `analysis` decorateurs |
| `RecordingTraceRecorder` | Capture la trace pour l'inspecter | Tests de tracabilite |

## `FakeLlmProvider`, la classe la plus rentable du projet

```java
var fake = new FakeLlmProvider()
        .repondra("{\"criterion\":\"architecture\",\"score\":7,...}")   // cas nominal
        .echouera(new LlmTimeoutException("delai depasse", null))       // puis un echec
        .repondra("Voici mon analyse :\n```json\n{...}\n```");          // puis du bruit
```

Une file de reponses permet de tester les sequences : deux echecs puis un succes
valide le comportement de `RetryingLlmProvider`.

## Les cas d'erreur a preparer (section 5)

Reponse vide · JSON entoure de texte · JSON dans un bloc markdown · JSON tronque ·
score hors bornes · mauvais critere · champ absent · `LlmTimeoutException` ·
`LlmUnavailableException` · deux reponses differentes a la meme requete.

Chacun correspond a une ligne de la liste des huit cas de la section 5 : votre
suite de tests devient la preuve que vous les avez traites.

## Regle

Ces classes sont du **code de test**, jamais utilisees en production. Elles ne
doivent apparaitre dans aucun import de `src/main/java`.
