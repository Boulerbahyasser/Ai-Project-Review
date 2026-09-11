# `analysis.result` — Ce qu'on obtient

Representation Java pure des resultats. Aucune dependance au LLM ni au format de
rapport : c'est la frontiere exigee par la section 7 du cahier des charges
(*separer les resultats, leur representation Java, et leur transformation en LaTeX*).

## Contenu

| Classe | Role |
|---|---|
| `ResultStatus` | `COMPLETED`, `PARTIAL`, `FAILED`, `SKIPPED` |
| `CriterionResult` | Resultat d'un critere : score, forces, faiblesses, recommandations, statut |
| `OverallScore` | Score global consolide + pourcentage |
| `AnalysisResult` | Resultat complet : projet, dates, modele, resultats, erreurs, trace |

## Pourquoi le statut est essentiel

C'est lui qui rend possible la **recuperation partielle** exigee en section 5 :
un critere en echec devient un `CriterionResult` marque `FAILED`, l'analyse
continue, et le rapport signale le critere non evalue.

## A ajouter ici

- Un champ de resultat (duree par critere, nombre d'appels au modele, niveau de confiance).

## Ne va pas ici

- Du texte formate, du LaTeX, du HTML : ces types sont neutres vis-a-vis du rendu.
- La regle de calcul du score global : elle est dans `analysis.aggregation`.

## Regle

Ces types sont **immuables** (`record`, listes recopiees). Un resultat produit ne
doit jamais etre modifie apres coup : il est persiste et rendu tel quel.
