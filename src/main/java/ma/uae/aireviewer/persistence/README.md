# `persistence` - Historique et cache

Reference : cahier des charges section 2 (point 10 : conserver un historique des analyses).

## Contenu attendu

- Interfaces de depot (historique des analyses, cache) : les couches hautes ne
  dependent que de ces interfaces.
- `entity/` : enregistrements persistes (identifiant d'analyse, projet, date, score,
  modele utilise, chemin du rapport).
- `json/` : implementation par fichiers JSON (aucune base de donnees requise).
- `cache/` : stockage du cache utilise par `llm` et par les analyseurs.

## Regles

- Ne jamais persister de cle d'API, de mot de passe ni de secret (section 12).
- Le format de stockage doit pouvoir changer sans impacter `application`.
