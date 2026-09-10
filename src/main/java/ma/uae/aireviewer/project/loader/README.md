# `project.loader` — Import du projet

Repond a la section 3.1 : import depuis une archive, un repertoire local, et
optionnellement un depot Git.

## Contenu

| Classe | Role |
|---|---|
| `ProjectSource` | Interface scellee : `LocalDirectory`, `Archive`, `GitRepository` |
| `ProjectLoader` | Contrat : `supports(source)` + `load(source)` |
| `ProjectLoaderFactory` | **Factory** : choisit le loader capable de traiter la source |
| `DirectoryProjectLoader` | Parcours d'un repertoire local |
| `ArchiveProjectLoader` | Extraction d'archive, puis delegation |
| `GitProjectLoader` | Clone superficiel, puis delegation |
| `ProjectImportException` | Source introuvable, archive corrompue, clone impossible |

## Points de securite non negociables

| Risque | Mesure |
|---|---|
| **Zip-slip** : une entree d'archive nommee `../../etc/passwd` | Resoudre chaque entree et **rejeter** tout chemin sortant du repertoire d'extraction |
| Hooks Git executes au clone | Clone superficiel, hooks desactives |
| Archive piegee (bombe de decompression) | Limiter la taille decompressee totale et le nombre d'entrees |
| Liens symboliques sortants | Ne pas les suivre |

Ces trois loaders manipulent du contenu fourni par un autre groupe : le cahier des
charges le declare **potentiellement malveillant** (section 8).

## Ajouter une source d'import

Une implementation de `ProjectLoader` + son enregistrement dans la fabrique.
Aucun fichier existant modifie.
