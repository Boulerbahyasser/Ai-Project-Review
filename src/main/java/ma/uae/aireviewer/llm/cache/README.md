# `llm.cache` — Eviter les appels inutiles

Repond a la derniere exigence de la section 4.2 : *« eviter les appels inutiles
autant que possible »*.

## Contenu

| Classe | Role |
|---|---|
| `CachingLlmProvider` | **Decorator** : une requete identique n'est pas renvoyee au modele |

## La cle de cache

Elle doit couvrir **tout ce qui influence la reponse** :

```
empreinte( modele + prompt systeme + prompt utilisateur + temperature + maxOutputTokens )
```

Oublier un element produit le pire des bugs : une reponse en cache servie alors
que les parametres ont change. Oublier la temperature, par exemple, renverrait la
reponse d'une execution deterministe a une execution creative.

## Ce que le cache apporte concretement

| Situation | Sans cache | Avec cache |
|---|---|---|
| Relancer apres correction d'un bug d'affichage | ~23 appels | 0 appel |
| Analyser deux fois le meme projet, autre profil | ~23 appels | ~12 appels (digest reutilise) |
| Developper et tester l'IHM | des appels a chaque essai | aucun |

Le troisieme cas est le plus utile au quotidien : il rend le developpement de
l'IHM possible sans attendre le modele a chaque clic.

## Ou vivent les donnees

Le stockage est dans `persistence.cache` (`CacheStore`). Ce package ne contient
que le decorateur : il ignore si le cache est en memoire, sur disque ou ailleurs.

## Attention

Ne jamais mettre en cache une reponse ayant echoue a la validation. Le decorateur
de cache se place **a l'interieur** de la resilience, pas autour.
