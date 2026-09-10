# `report.model` — Le rapport en representation Java

Repond a l'exigence en gras de la section 7 : **la structure du document ne doit
pas dependre uniquement du LLM**. Le modele fournit du texte ; le squelette du
rapport est construit ici, en Java.

## Contenu

| Classe | Role |
|---|---|
| `ReportHeader` | Projet, date, configuration, profil, modele utilise |
| `ReportSection` | Titre, paragraphes, listes a puces |
| `ScoreRow` | Une ligne du tableau de synthese |
| `ScoreTable` | Le tableau + total et maximum |
| `EvaluationReport` | En-tete + tableau + sections + synthese |
| `ReportBuilder` | **Builder** : construction progressive, validation au `build()` |

## Ce que le rapport doit contenir (section 7)

Identification du projet, date d'analyse, configuration utilisee, modele employe,
criteres evalues, scores obtenus, forces, faiblesses, problemes identifies,
recommandations, synthese globale.

Verifiez cette liste avant la remise : elle est enumeree explicitement dans le
cahier des charges et donc facile a controler pour le correcteur.

## Aucun format ici

Ces classes ne contiennent **ni LaTeX, ni HTML, ni markdown**. Un `ReportSection`
porte du texte brut. Le formatage appartient a `report.render`. C'est la
deuxieme des trois etapes que la section 7 demande de separer :

```
1. resultats d'evaluation  (analysis.result)
2. representation Java     (ce package)
3. transformation LaTeX    (report.render + report.latex)
```

## Pourquoi un Builder

Un rapport s'assemble par morceaux, dans un ordre qui depend du profil de
criteres, avec des parties optionnelles (une section par critere evalue, absente
si le critere a ete ignore). Le `build()` verifie qu'aucune partie obligatoire ne
manque avant de sceller l'objet.
