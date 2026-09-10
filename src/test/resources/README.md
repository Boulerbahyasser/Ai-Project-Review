# `src/test/resources` — Donnees de test

| Chemin | Contenu |
|---|---|
| `projects/` | Projets d'exemple servant d'entree aux tests d'import et d'analyse |

## Les projets d'exemple a preparer

| Cas | Ce qu'il valide |
|---|---|
| `sample-project/` | Projet Maven normal : import, classification, arborescence |
| Un projet sans tests | `TestPresenceAnalyzer` doit donner un score bas |
| Un projet sans Dockerfile | `DockerQualityAnalyzer` doit rendre `SKIPPED`, pas echouer |
| Un fichier avec une tentative d'injection | La defense de `security.prompt` doit la neutraliser |
| Une archive piegee (chemin `../`) | `ArchiveProjectLoader` doit la rejeter (zip-slip) |

Les deux derniers sont les plus importants : ils prouvent que les mesures de
securite des sections 8.1 et 8.2 fonctionnent reellement, au lieu d'etre
seulement decrites dans le rapport.

## Regle

Ces projets sont de **fausses** donnees, volontairement petites. Ne pas y placer
le projet d'un autre groupe : les tests doivent rester rapides et reproductibles.
