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
| **Zip-slip** : une entree d'archive nommee `../../etc/passwd` | Rejete avant extraction par `ArchiveExtractor` (`security.archive`) |
| Hooks Git executes au clone | Clone superficiel, hooks desactives |
| Archive piegee (bombe de decompression) | Bornee par `ArchiveExtractor` (taille et nombre d'entrees) |
| Liens symboliques sortants | Rejetes par `ArchiveExtractor` |

Ces trois loaders manipulent du contenu fourni par un autre groupe : le cahier des
charges le declare **potentiellement malveillant** (section 8).

`ArchiveProjectLoader` ne decompresse plus rien lui-meme : il delegue a
`ArchiveExtractor` (conteneur Docker dedie, cf. `security/archive/README.md`),
puis parcourt le resultat comme un repertoire local. C'est le seul point du
package qui depend de `security`.

## Ajouter une source d'import

Une implementation de `ProjectLoader` + son enregistrement dans la fabrique.
Aucun fichier existant modifie.
