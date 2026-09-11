# `persistence.json` — Stockage en fichiers JSON

## Contenu

| Classe | Role |
|---|---|
| `JsonAnalysisHistoryRepository` | Historique persiste en fichiers JSON |

## Pourquoi JSON et pas une base de donnees

| Critere | JSON | Base de donnees |
|---|---|---|
| Installation pour un correcteur | aucune | serveur ou pilote a installer |
| Lisibilite du contenu | directe | requete necessaire |
| Volume attendu | quelques dizaines d'analyses | inutilement dimensionne |

Le cahier des charges n'exige aucune base de donnees. Un choix simple et justifie
vaut mieux qu'une technologie impressionnante et non maitrisee — la soutenance
exige de pouvoir defendre chaque choix (section 14).

## A implementer avec soin

- **Creer le repertoire** avant d'ecrire : `out/history/` n'existe pas au clone
  (seul `out/.gitkeep` est versionne). Oubli classique produisant un
  `NoSuchFileException` au premier lancement.
- **Ecriture atomique** : ecrire dans un fichier temporaire puis renommer, pour
  qu'une interruption ne laisse pas un JSON tronque.
- **Dates** : Jackson a besoin du module `jsr310` pour `Instant` — il est deja
  declare dans le `pom.xml`.

## Si le format change

Seule cette classe est concernee : `application` ne depend que de l'interface
`AnalysisHistoryRepository`. Remplacer JSON par SQLite ne touche rien d'autre.
