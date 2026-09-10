# `application.dto` — Objets d'echange avec l'IHM

## Contenu

| Classe | Role |
|---|---|
| `ImportProjectRequest` | Source d'import demandee |
| `RunAnalysisRequest` | Projet, profil, fournisseur |
| `GenerateReportRequest` | Analyse, format, compilation PDF |
| `AnalysisSummary` | Vue simplifiee d'un resultat pour l'affichage |

## Pourquoi ne pas exposer directement les types internes

Si l'IHM manipulait `AnalysisResult` — avec sa trace complete, ses statuts, ses
listes imbriquees — alors :

1. Tout changement du modele d'analyse casserait l'IHM.
2. L'IHM serait tentee de calculer a partir de ces donnees, donc de contenir de
   la logique metier (sanctionne en section 16.2).

`AnalysisSummary` ne porte que ce qu'un ecran affiche : score, maximum, si
l'analyse est partielle, combien de criteres ont echoue.

## Regle

Ce sont des `record` immuables, sans methode metier. Un DTO qui gagne des
methodes de calcul n'est plus un DTO : la logique appartient au sous-systeme
correspondant.

## Ne va pas ici

Les entites internes (`SoftwareProject`, `AnalysisResult`, `Criterion`).
Note : `ImportProjectRequest` transporte un `ProjectSource` de `project` — c'est
un choix assume, ce type etant deja un simple descripteur de source.
