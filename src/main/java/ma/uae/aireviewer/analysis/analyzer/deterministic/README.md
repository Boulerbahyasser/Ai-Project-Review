# `analysis.analyzer.deterministic` — Analyses calculees

Analyses que le programme effectue **seul**, sans appel reseau. Elles repondent au
premier probleme d'architecture pose par la section 1 du cahier des charges :
*combiner analyses deterministes et analyses generees par IA*.

## Contenu

| Classe | Ce qu'elle mesure |
|---|---|
| `ProjectOrganizationAnalyzer` | Arborescence standard, fichier de build, README |
| `TestPresenceAnalyzer` | Ratio fichiers de test / fichiers source |
| `CodeDuplicationAnalyzer` | Empreintes de blocs de lignes normalisees, collisions |
| `DockerQualityAnalyzer` | `USER` non root, tag d'image fixe, build multi-etapes |

## Pourquoi ces analyses comptent autant que celles du LLM

1. Elles sont **reproductibles** : deux executions donnent le meme score. Un modele, non.
2. Elles fonctionnent **sans modele disponible** : l'application reste utile si le
   serveur local est arrete (section 5).
3. Elles sont **verifiables** : vous pouvez expliquer exactement pourquoi le score vaut 7.

Un projet qui ne reposerait que sur le LLM serait fragile et non reproductible.
Le mentionner au rapport est un point gagne.

## A ajouter ici

Tout critere mesurable par calcul : longueur des methodes, profondeur
d'imbrication, nombre de dependances entre packages, presence de Javadoc,
respect des conventions de nommage.

## Regle

Ces classes lisent le contenu des fichiers du projet analyse : ce contenu est
**non fiable**. Ne jamais l'executer, ne jamais l'interpreter — seulement le lire
comme du texte.
