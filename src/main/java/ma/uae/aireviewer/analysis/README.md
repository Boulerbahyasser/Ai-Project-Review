# `analysis` - Moteur d'evaluation

Reference : cahier des charges section 3.3. Exigence structurante : **ajouter un
critere ne doit pas obliger a modifier les composants existants**.

## Contenu attendu

- Moteur d'analyse (interface + implementation) : resout les analyseurs du profil,
  les execute, publie la progression, consolide.
- `criterion/` : definition d'un critere (identifiant, libelle, note maximale, poids,
  nature deterministe / LLM / hybride) et profils de criteres charges depuis `config/profiles/`.
- `analyzer/` : contrat d'analyseur + classe abstraite commune (pattern Template Method).
  - `deterministic/` : analyses calculees par le programme (organisation, tests,
    duplication, qualite du Dockerfile).
  - `ai/` : analyses confiees a un modele de langage.
  - `decorator/` : comportements ajoutes autour d'un analyseur (trace, tolerance
    aux pannes, cache) - pattern Decorator.
- `result/` : resultat par critere et resultat global (representation Java pure).
- `aggregation/` : consolidation des scores (pattern Strategy).
- `trace/` : tracabilite d'une analyse (section 12).

## Regles

- Un analyseur en echec ne doit pas faire echouer l'analyse entiere :
  il produit un resultat marque en echec (recuperation partielle, section 5).
- `analysis` ne connait le LLM qu'a travers l'interface de `llm`. Aucun appel HTTP ici.
- Aucun `import ma.uae.aireviewer.ui.*`.

## Ajouter un critere

1. Declarer le critere dans `config/profiles/*.yaml`.
2. Si deterministe ou hybride : ajouter un analyseur dans le sous-package adequat.
3. Enregistrer sa fabrique. Aucun autre fichier ne doit changer.
