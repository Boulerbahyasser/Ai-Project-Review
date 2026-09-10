# `analysis.trace` — Tracabilite d'une analyse

Repond a la section 12 du cahier des charges : conserver de quoi comprendre
comment une analyse s'est deroulee.

## Contenu

| Classe | Role |
|---|---|
| `TraceEvent` | Horodatage, categorie, message, duree, attributs |
| `TraceRecorder` | Contrat de collecte — injecte, donc substituable en test |
| `InMemoryTraceRecorder` | Trace en memoire pour la duree de l'analyse |

## Ce qui doit etre trace (section 12)

Debut et fin d'analyse, criteres executes, erreurs rencontrees, modele utilise,
duree des operations, nombre d'appels effectues, resultats intermediaires pertinents.

## Ce qui ne doit JAMAIS etre trace

Cles d'API, mots de passe, secrets, donnees confidentielles. Tout texte
susceptible d'en contenir passe par `security.secret.SecretRedactor` **avant**
d'etre enregistre. C'est une exigence explicite du cahier des charges.

## A ajouter ici

- Une implementation qui ecrit la trace au fil de l'eau dans un fichier.
- Un compteur d'appels et de jetons consommes (extension de la section 18).

## Ne va pas ici

- La journalisation technique de debogage : utilisez SLF4J directement.
  La trace est une **donnee metier**, persistee avec le resultat et exploitee
  par le rapport ; le journal est un outil de developpeur.
