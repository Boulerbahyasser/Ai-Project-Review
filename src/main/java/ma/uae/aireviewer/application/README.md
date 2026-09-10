# `application` - Cas d'utilisation

Couche d'orchestration : elle expose a l'IHM un point d'entree simple (Facade) et
coordonne les autres sous-systemes.

## Contenu attendu

- Facade d'acces unique pour l'IHM.
- `usecase/` : un cas d'utilisation par action metier (importer un projet,
  lancer une analyse, generer un rapport, consulter l'historique).
- `dto/` : objets d'echange avec l'IHM (jamais les entites internes brutes).
- `event/` : evenements de progression et d'erreur (pattern Observer, section 6).
- `command/` : representation des operations executables sur un projet
  (pattern Command : execution hors du thread IHM, file d'attente, journalisation).

## Interdits

- Aucun code JavaFX, aucun code HTTP, aucun LaTeX.
- Pas de regle de notation : elle appartient a `analysis`.
