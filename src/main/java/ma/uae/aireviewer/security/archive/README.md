# `security.archive` — Extraction isolee d'archives

Meme principe que `security.sandbox` (section 8) applique a l'import : une archive
`.zip` deposee par l'utilisateur est un **contenu non fiable** tant qu'elle n'a
pas ete extraite dans un conteneur jetable, sans reseau, en dehors de la machine
hote.

## Contenu

| Classe | Role |
|---|---|
| `ArchiveExtractor` | Contrat : `extract(archive, destination) -> ExtractionResult` |
| `ExtractionStatus` | Issue : succes, archive invalide, limite depassee, delai depasse, Docker indisponible |
| `ExtractionResult` | Statut, destination, code de sortie, message |
| `DockerArchiveExtractor` | Extraction dans un conteneur dedie, supprime apres usage |
| `DisabledArchiveExtractor` | Aucun moteur Docker disponible : renvoie `DOCKER_UNAVAILABLE`, ne leve jamais |

## L'architecture imposee

```
Machine virtuelle dediee a l'experimentation
   +-- Conteneur Docker dedie, sans reseau
         +-- Archive non fiable (entree utilisateur)
```

`DockerArchiveExtractor` est le seul point du systeme qui sait que Docker existe.
Le reste de l'application ne voit que `ArchiveExtractor`.

## Deroulement dans le conteneur (un seul script, cf. `DockerArchiveExtractor`)

1. `unzip -Z1` liste les entrees sans rien ecrire. Une archive corrompue ou
   illisible s'arrete ici (`INVALID_ARCHIVE`).
2. Chaque nom est controle avant extraction : chemin absolu, `..`, ou antislash
   -> rejet (`INVALID_ARCHIVE`), zero octet ecrit.
3. Le nombre d'entrees est compare a `maxEntryCount` -> rejet si depasse
   (`LIMIT_EXCEEDED`), toujours avant extraction.
4. L'extraction reelle se fait dans `/work`, un `tmpfs` dont la taille est fixee
   a `maxExtractedSizeMb`. Si l'archive decompressee est plus grosse, `unzip`
   echoue lui-meme (code 50, disque plein) -> `LIMIT_EXCEEDED`. La bombe zip est
   ainsi bornee par le systeme de fichiers, pas seulement par une verification
   applicative.
5. Apres extraction, un `find -type l` rejette toute archive ayant produit un
   lien symbolique (`INVALID_ARCHIVE`).
6. La taille totale extraite est revérifiée explicitement (`du -sk`), en plus
   de la limite `tmpfs`.
7. Seule une archive ayant franchi toutes ces etapes est copiee vers `/output`,
   qui est **le seul montage en ecriture** du conteneur (le repertoire de
   destination fourni par l'appelant, monte depuis l'hote).

Si une seule etape echoue, rien n'est jamais copie vers `/output` : le
repertoire de destination reste vide ou inchange.

## Les options `docker run` correspondant aux restrictions

```
--rm                                 conteneur supprime apres usage
--network none                       aucun acces reseau
--read-only                          systeme de fichiers racine en lecture seule
--cap-drop ALL                       aucune capacite privilegiee
--security-opt no-new-privileges
--user <unzip.user>                  utilisateur non root
--memory / --pids-limit              bornes memoire et nombre de processus
--tmpfs /work:size=<maxExtractedSizeMb>m   espace d'extraction borne (bombe zip)
-v <archive>:/input/archive.zip:ro   archive montee en LECTURE SEULE
-v <destination>:/output:rw          seul montage en ecriture
```

Le delai maximal (`unzip.timeoutSeconds`) est applique cote Java par
`DockerArchiveExtractor`, exactement comme `DockerSandboxRunner` : `waitFor`
avec delai, puis `destroyForcibly` si depasse.

## Pourquoi `DisabledArchiveExtractor` ne leve pas d'exception

`DisabledSandboxRunner` leve une exception parce que l'execution de code est
un choix qui doit rester conscient et desactive par defaut. L'extraction
d'archive n'est pas un choix similaire : c'est une etape necessaire pour
analyser un projet importe en `.zip`. L'absence de Docker est donc un **echec
attendu et gere**, pas une politique refusee : `DisabledArchiveExtractor`
renvoie `DOCKER_UNAVAILABLE`, au meme titre qu'une archive corrompue renvoie
`INVALID_ARCHIVE`. Le reste de l'application traite les deux de la meme
maniere, un resultat, jamais une exception — c'est pour cela qu'`ExtractionResult`
distingue explicitement ce cas plutot que de le laisser remonter en erreur.

## Menaces couvertes (section 8)

| Menace | Parade |
|---|---|
| Bombe zip | `tmpfs` borne + `maxExtractedSizeMb` + `maxEntryCount`, verifies avant **et** apres extraction |
| Zip-slip (`../../etc/passwd`) | Noms rejetes avant toute ecriture ; `/output` est le seul chemin d'ecriture possible |
| Nom de fichier malveillant (antislash, chemin absolu, lien symbolique) | Rejet sur les noms + rejet post-extraction des liens symboliques |
| Archive hostile a extraction longue | Delai maximal applique cote Java (`unzip.timeoutSeconds`) |

## Regle

`DockerArchiveExtractor` ne fait confiance a aucune metadonnee de l'archive
avant de l'avoir verifiee lui-meme. Comme `LeastPrivilegePolicy`, il **refuse**,
il ne corrige pas : un nom de fichier suspect rejette l'archive entiere plutot
que de tenter de le nettoyer.

## Ce qui n'est volontairement pas fait ici

`UnzipConfig` (package `configuration`) existe pour porter les parametres de la
section `unzip:` de `config/application.yaml`, mais rien ne le construit encore
a partir d'`AppConfiguration` : exactement comme `SandboxConfig` aujourd'hui,
aucune classe ne convertit la configuration chargee en instance prete a
l'emploi. Cette conversion touche `AppConfiguration` et `YamlConfigurationLoader`,
deux fichiers volontairement laisses inchanges ici (voir la demande d'origine) ;
elle est a faire dans la meme passe que l'equivalent pour `SandboxConfig`.
