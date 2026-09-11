# `report` - Generation du rapport

Reference : cahier des charges section 7. Exigence : la **structure du document ne
depend pas du LLM**. Le modele peut fournir du texte, pas le squelette du rapport.

## Contenu attendu

- `model/` : representation Java du rapport (en-tete d'identification, sections,
  tableau de synthese des scores, conclusion) construite avec un pattern Builder.
- `assembly/` : transformation resultat d'analyse -> modele de rapport.
- `render/` : rendu vers un format (pattern Strategy : LaTeX obligatoire, HTML optionnel).
- `latex/` : gabarit et **echappement LaTeX** du texte issu du projet analyse ou du LLM.
- `compile/` : compilation optionnelle `.tex` -> `.pdf`, isolee dans un conteneur
  (la compilation ne doit pas compromettre la securite de l'application).

## Trois etapes a garder separees

1. resultats d'evaluation ; 2. leur representation Java ; 3. leur transformation en LaTeX.

## Regles

- Echapper systematiquement les caracteres speciaux LaTeX venant du projet analyse.
- Compilation sans `--shell-escape`.
- Livrable attendu : `evaluation.tex` genere par le programme (et si possible son PDF).
