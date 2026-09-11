# `analysis.aggregation` — Consolidation des scores

Transforme une liste de resultats par critere en un score global unique.
Isole en pattern Strategy parce que la regle de notation est un choix
pedagogique qui peut evoluer sans toucher au moteur.

## Contenu

| Classe | Role |
|---|---|
| `ScoreAggregator` | Contrat : `aggregate(resultats, profil) -> OverallScore` |
| `WeightedAverageAggregator` | Moyenne ponderee par le poids declare de chaque critere |

## Decision en vigueur, a justifier au rapport

Les criteres `FAILED` et `SKIPPED` sont exclus **du numerateur et du denominateur** :
un critere non evalue ne penalise pas la note. L'alternative — le compter a zero —
punirait un projet pour une panne de modele, ce qui n'a pas de sens.

## A ajouter ici

- Une regle par categorie (score par groupe de criteres).
- Un plancher eliminatoire (note nulle si un critere critique echoue).
- Une normalisation sur 20.

## Ne va pas ici

- Le score d'un critere individuel : il vient de l'analyseur.
- La mise en forme du tableau de synthese : c'est `report`.
