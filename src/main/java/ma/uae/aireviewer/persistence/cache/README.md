# `persistence.cache` — Stockage du cache

## Contenu

| Classe | Role |
|---|---|
| `CacheStore` | Contrat : `get(cle)` + `put(cle, valeur)` |
| `FileSystemCacheStore` | Un fichier par cle, sous `out/cache/` |

## Qui l'utilise

| Consommateur | Ce qu'il met en cache |
|---|---|
| `llm.cache.CachingLlmProvider` | Les reponses du modele par empreinte de requete |
| `analysis.analyzer.decorator.CachingAnalyzer` | Les resultats de critere par empreinte de fichiers |

## Pourquoi une interface aussi minimale

Deux methodes, `String -> String`. C'est volontaire : le cache doit rester
remplacable (memoire, disque, aucun cache en test) sans que ses deux consommateurs
changent. Une interface plus riche (expiration, statistiques, invalidation par
prefixe) les coupleraient a une implementation particuliere.

## Points d'attention a l'implementation

- **Nom de fichier** : une cle est une empreinte, donc deja sure ; mais ne
  concatenez jamais un chemin fourni par l'utilisateur dans un nom de fichier.
- **Creer `out/cache/`** avant d'ecrire.
- **Taille** : prevoir une purge, ou au moins documenter que le repertoire croit
  indefiniment. `out/` est ignore par Git, donc l'utilisateur peut le supprimer
  sans risque — dites-le dans le README racine.

## Extension possible (section 18)

Une estimation du cout ou du nombre de jetons economises grace au cache : c'est
une des extensions optionnelles citees par le cahier des charges, et elle se
mesure naturellement ici.
