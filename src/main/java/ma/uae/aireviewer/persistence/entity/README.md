# `persistence.entity` — Ce qui est stocke

## Contenu

| Classe | Role |
|---|---|
| `AnalysisRecord` | Identifiant, projet, date, score, profil, modele, chemin du rapport |

## Pourquoi une entite distincte de `AnalysisResult`

`AnalysisResult` contient tout : les resultats par critere, les forces et
faiblesses, la trace complete. Le stocker entierement dans l'historique
rendrait la liste des analyses lourde a charger pour un simple affichage.

`AnalysisRecord` est un **resume** : ce qu'il faut pour afficher une ligne
d'historique et retrouver le rapport complet sur disque.

## Regle absolue

Aucun secret ici. Ni cle d'API, ni jeton, ni mot de passe (section 12).
Le champ `modelUsed` contient le nom du modele, jamais l'URL avec sa cle.

## Si vous evoluez vers un stockage complet

Deux options a documenter dans `docs/DECISIONS.md` :

1. `AnalysisRecord` en index + le `AnalysisResult` complet dans un fichier a part.
2. Tout dans un seul document JSON par analyse.

L'option 1 garde l'affichage de l'historique rapide ; l'option 2 est plus simple.
