# `llm.context` — Gestion du contexte

Repond a la section 4.2, le vrai probleme technique du projet : *« un projet peut
contenir bien plus de code que ce qui peut etre envoye en une seule requete »*.

## Contenu

| Classe | Role |
|---|---|
| `CodeChunk` | Fragment de code destine a une requete |
| `ContextBudget` | Enveloppe autorisee : caracteres, fichiers, fragments par critere |
| `ChunkingStrategy` | **Strategy** : maniere de decouper |
| `WholeFileChunkingStrategy` | Un fragment par fichier, tant que le budget le permet |
| `SlidingWindowChunkingStrategy` | Fenetres de lignes avec recouvrement, pour les gros fichiers |
| `ContextAssembler` | Orchestration : decoupage, puis resume si le volume reste excessif |
| `IntermediateSummarizer` | Reduit plusieurs fragments en une synthese reinjectable |

## L'arithmetique a garder en tete

Projet realiste : ~60 fichiers retenus, ~380 000 caracteres. Un appel accepte
confortablement ~40 000 caracteres, soit ~10 lots pour couvrir le projet. Avec
12 criteres evalues par modele, l'approche naive donne **120 appels**.

Strategie recommandee, en deux phases :

```
Phase 1 (une fois)   : 10 resumes + 1 consolidation  ->  un digest (~8 000 car.)
Phase 2 (par critere): digest + 2 ou 3 extraits      ->  12 appels
                                            total :  23 appels au lieu de 120
```

Le digest est **reutilisable** : relancer avec un autre profil ne redigere pas le
projet. C'est ce que le cache exploite.

## Obligation du cahier des charges

*« La strategie adoptee doit etre expliquee dans votre rapport »* (section 4.2).
Documentez-la dans `docs/DECISIONS.md` au fur et a mesure.

## Ce que ce package ne fait PAS

Il ne **choisit pas** les fichiers : la selection est faite en amont par `project`
(regles d'inclusion) puis par `analysis` (pertinence par critere). Ici on ne
decide que ce qui **tient dans une requete**.
