# `templates/` — Gabarit du rapport

## Contenu

| Fichier | Role |
|---|---|
| `report.tex.template` | Squelette LaTeX du rapport d'evaluation |

## Pourquoi ce fichier est la reponse a une exigence en gras

Section 7 : **la structure du document ne doit pas dependre uniquement du LLM**.

Ce gabarit *est* cette structure : preambule, page de titre, section
d'identification, tableau de synthese, evaluation detaillee, synthese generale.
Il est ecrit par vous, versionne, et identique quel que soit le modele utilise.
Le modele ne fournit que du texte inseré dans les emplacements prevus.

## Les variables

`{{projectName}}` · `{{analysisDate}}` · `{{configurationSummary}}` ·
`{{modelUsed}}` · `{{scoreTable}}` · `{{sections}}` · `{{summary}}`

## Regle de securite

Tout texte substitue provenant du projet analyse ou du modele doit avoir traverse
`report.latex.LatexEscaper`. Sans echappement, un commentaire du type
`\input{/etc/passwd}` dans le code analyse serait interprete a la compilation
(sections 7 et 8).

## Extension possible (section 18)

Le cahier des charges cite *« tableaux et graphiques dans le rapport »* parmi les
extensions. Un graphique en barres des scores par critere se fait en LaTeX pur
avec `pgfplots`, sans dependance supplementaire — a ajouter ici.
