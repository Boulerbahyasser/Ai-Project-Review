# `llm.parsing` — Lecture et validation des reponses

Repond a la phrase de la section 4.3 : *« votre programme doit valider les
reponses obtenues avant de les utiliser »*. C'est la frontiere entre le texte
produit par un modele et le modele metier.

## Contenu

| Classe | Role |
|---|---|
| `EvaluationPayload` | Le contrat de sortie attendu : le schema JSON de la section 4.3 |
| `ResponseParser` | Contrat : reponse brute -> structure, ou echec explicite |
| `JsonResponseParser` | Extraction tolerante + application des validateurs |
| `ResponseValidator` | Contrat d'une regle de validation |
| `ScoreRangeValidator` | Refuse un score hors bornes ou une note maximale absurde |
| `RequiredFieldsValidator` | Refuse un critere absent ou different de celui demande |

## Tolerant sur la forme, strict sur le fond

Les modeles ajoutent souvent du texte autour du JSON, ou l'enveloppent dans un
bloc markdown. Le parseur doit **extraire** l'objet plutot que rejeter la
reponse : c'est de la forme.

En revanche un score de 15/10, un critere qui ne correspond pas, un champ
manquant sont des erreurs de **fond**. Elles sont rejetees par une
`LlmInvalidResponseException`, jamais corrigees en silence : un score devine
serait une note fausse dans un rapport d'evaluation.

## Ajouter une regle de validation

Une implementation de `ResponseValidator`, ajoutee a la liste passee au parseur.
Aucune classe existante modifiee.

## Les cas a couvrir en test (section 11)

Reponse vide · JSON dans un bloc markdown · texte avant et apres · JSON tronque
par la limite de jetons · score hors bornes · mauvais critere · champ absent ·
listes nulles. Tous se testent avec `FakeLlmProvider`, sans reseau.
