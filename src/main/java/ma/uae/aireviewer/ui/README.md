# `ui` - Interface graphique (JavaFX)

Reference : cahier des charges section 3.2. La separation IHM / logique metier est
**evaluee specifiquement**. Une IHM qui contient la logique est explicitement
citee comme probleme (section 16.2).

## Contenu attendu

- `view/` : composants d'affichage (selection de projet, arbre du projet,
  choix de configuration, barre de progression, panneau d'erreurs, resultats).
- `controller/` : reaction aux actions utilisateur ; delegue immediatement a `application`.
- `viewmodel/` : etat d'affichage observable, sans regle metier.
- `bridge/` : adaptation des evenements de `application.event` vers le thread JavaFX.

## Interdits

- Aucun appel HTTP, aucun `docker`, aucun `pdflatex`.
- Aucun calcul de score, aucun parcours de fichiers du projet analyse.
- Aucun `import ma.uae.aireviewer.llm.*` ni `...analysis.analyzer.*`.

## Regle

L'IHM ne parle qu'a la facade de `application` et n'ecoute que ses evenements.
