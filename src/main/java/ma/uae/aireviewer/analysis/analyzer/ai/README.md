# `analysis.analyzer.ai` — Analyses confiees a un modele

Les seules classes de `analysis` qui dialoguent avec un modele de langage, et
uniquement a travers l'interface `LlmProvider`. Changer de fournisseur ne les
modifie pas.

## Contenu

| Classe | Role |
|---|---|
| `LlmCriterionAnalyzer` | Evalue un critere via un modele : contexte -> prompt -> appel -> validation -> resultat |
| `HybridCriterionAnalyzer` | Compose une mesure deterministe et une appreciation par modele, puis fusionne |

## Le deroulement de `LlmCriterionAnalyzer`

```
1. ContextAssembler        assemble les fragments dans le budget de contexte
2. CriterionPromptFactory  construit la requete (role, critere, code, format attendu)
3. LlmProvider.ask()       envoie — reessais et cache sont deja dans le fournisseur
4. ResponseParser.parse()  extrait et VALIDE le JSON
5. conversion              EvaluationPayload -> CriterionResult
```

Les quatre dependances arrivent par le constructeur : en test, on injecte un faux
fournisseur et l'analyseur se teste sans reseau (section 11).

## Ne va pas ici

- La construction du prompt : elle est dans `llm.prompt`.
- Le parsing de la reponse : il est dans `llm.parsing`.
- La gestion des reessais : elle est dans `llm.resilience`.

Cette classe **orchestre**, elle n'implemente aucun de ces quatre mecanismes.

## Sur `HybridCriterionAnalyzer`

La regle de fusion est une decision a consigner dans `docs/DECISIONS.md` :
moyenne des deux scores ? le plus faible des deux ? le deterministe qui plafonne
le score du modele ? Chaque option se defend, aucune n'est neutre.
