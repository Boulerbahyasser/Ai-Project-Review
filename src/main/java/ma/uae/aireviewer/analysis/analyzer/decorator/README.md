# `analysis.analyzer.decorator` — Comportements transverses

Pattern **Decorator**. Chaque classe implemente `Analyzer`, en enveloppe un autre,
ajoute son comportement et delegue le reste. Comme le type est identique, on empile.

## Contenu

| Classe | Apport | Section |
|---|---|---|
| `AnalyzerDecorator` | Base commune : delegue tout par defaut | — |
| `FailSafeAnalyzer` | Toute exception devient un resultat `FAILED` | 5 |
| `TracingAnalyzer` | Horodate et chronometre dans la trace | 12 |
| `CachingAnalyzer` | Evite de reevaluer un contenu inchange | 4.2 |

## L'ordre d'empilement, a ne pas improviser

```java
new FailSafeAnalyzer( new TracingAnalyzer( new CachingAnalyzer( analyseurReel )))
```

- `Caching` au plus pres : il court-circuite le travail reel.
- `Tracing` au-dessus : il voit les succes de cache et les durees observees.
- `FailSafe` en dernier rempart : il attrape aussi une panne du cache ou de la trace.

**Inverser `FailSafe` et `Caching` mettrait un resultat en echec dans le cache** —
bug tres difficile a diagnostiquer. Cet ordre est une decision d'architecture :
consignez-la dans `docs/DECISIONS.md`.

## Pourquoi `FailSafeAnalyzer` est la classe la plus importante du package

Onze lignes qui realisent a elles seules la recuperation partielle exigee par la
section 5 : un critere qui echoue ne fait plus tomber l'analyse entiere.

## A ajouter ici

Limitation de debit, mesure du cout en jetons, execution paralelle,
nouvelle tentative avec un contexte reduit.

## Regle

Un decorateur **enrichit** le comportement, il ne le **change** pas : il doit
rester substituable a ce qu'il enveloppe.
