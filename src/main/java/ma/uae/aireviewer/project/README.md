# `project` - Import et representation du projet analyse

Reference : cahier des charges section 3.1.

## Contenu attendu

- `model/` : representation de l'arborescence (pattern Composite : repertoire / fichier),
  metadonnees, types de fichiers.
- `loader/` : import depuis un repertoire local, une archive, un depot Git
  (une implementation par source + une fabrique).
- `classification/` : identification du type de fichier (Java, config, Maven/Gradle,
  Docker, documentation, tests, scripts, ressources).
- `selection/` : regles d'inclusion / exclusion des fichiers envoyes au LLM
  (pattern Strategy : tous les fichiers n'ont pas a etre envoyes).

## Points de vigilance securite

- Le contenu importe est **non fiable** (section 8).
- Extraction d'archive : rejeter toute entree qui sort du repertoire cible (zip-slip).
- Import Git : clone superficiel, aucune execution de hook.

## Interdits

- Aucun appel LLM, aucune notation, aucun rendu de rapport.
