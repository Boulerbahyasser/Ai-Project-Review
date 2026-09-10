# `report.assembly` — Du resultat d'analyse au rapport

Le pont entre `analysis.result` et `report.model`. C'est ici que se decide **ce
qui figure** dans le rapport et dans quel ordre.

## Contenu

| Classe | Role |
|---|---|
| `ReportAssembler` | Contrat : `assemble(AnalysisResult) -> EvaluationReport` |
| `DefaultReportAssembler` | En-tete, tableau des scores, une section par critere, synthese |

## Decisions a prendre ici, et a consigner

| Question | Options |
|---|---|
| Que faire d'un critere `FAILED` ? | L'omettre, ou l'afficher avec la mention « non evalue » et la cause |
| Ordre des sections | Ordre du profil, ou par score croissant (les faiblesses d'abord) |
| La synthese globale | Redigee par le programme a partir des scores, ou demandee au modele |

Sur le premier point : **afficher** le critere non evalue est preferable. Un
rapport qui masque ses lacunes est trompeur, et la section 5 valorise la
recuperation partielle explicite.

Sur le troisieme : si la synthese vient du modele, elle reste **un contenu insere
dans une structure fixe**. Le squelette du document ne doit jamais dependre de lui.

## Pourquoi une classe separee du rendu

Le meme `EvaluationReport` doit pouvoir etre rendu en LaTeX **et** en HTML.
Si l'assemblage etait dans le moteur de rendu, il serait duplique a chaque format.
