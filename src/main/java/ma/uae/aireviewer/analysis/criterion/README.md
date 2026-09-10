# `analysis.criterion` — Ce qu'on evalue

Definition d'un critere d'evaluation et regroupement en profils selectionnables
depuis l'IHM. Un critere est une **donnee de configuration**, pas une classe :
c'est ce qui permet d'en ajouter un sans recompiler.

## Contenu

| Classe | Role |
|---|---|
| `CriterionId` | Identifiant stable, utilise en config, en cache et dans le rapport |
| `Criterion` | Libelle, description, note maximale, poids, nature |
| `AnalysisKind` | `DETERMINISTIC`, `LLM` ou `HYBRID` — decide quel analyseur construire |
| `CriterionProfile` | Ensemble nomme de criteres + calcul du total pondere |
| `CriterionCatalog` | Contrat d'acces aux profils disponibles |
| `YamlCriterionCatalog` | Lecture des profils depuis `config/profiles/*.yaml` |

## A ajouter ici

- Un nouveau champ de critere (seuil, categorie, criticite).
- Une autre source de profils (JSON, base de donnees) : une implementation de `CriterionCatalog`.

## Ne va pas ici

- La logique d'evaluation : elle est dans `analysis.analyzer`.
- Les criteres eux-memes en dur dans le code Java : ils vivent dans `config/profiles/`.

## Attention

`AnalysisKind` ne doit etre lu **que** par les fabriques d'analyseurs, au moment de
la construction. Un `if (criterion.kind() == LLM)` dans une methode metier
transformerait cet enum en defaut de conception.

Reference : cahier des charges section 3.3.
