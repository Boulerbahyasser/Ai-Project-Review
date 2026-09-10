# `report.compile` — Compilation en PDF

La section 7 qualifie la compilation PDF de *« fonctionnalite interessante »*,
donc optionnelle — mais assortie d'une condition : elle *« ne doit pas
compromettre la securite de l'application »*.

## Contenu

| Classe | Role |
|---|---|
| `PdfCompiler` | Contrat : `compile(fichier.tex) -> Optional<chemin PDF>` |
| `DockerLatexCompiler` | Compilation isolee dans un conteneur |
| `NoOpPdfCompiler` | **Par defaut** : le `.tex` est produit, aucun PDF |

## Pourquoi `Optional` en retour

L'absence de PDF n'est pas une erreur : c'est le comportement par defaut, et le
livrable exige par la section 13.4 est le `.tex` (*« et si possible »* son PDF).
Une analyse ne doit jamais echouer parce que LaTeX n'est pas installe.

## Pourquoi compiler dans un conteneur

Le `.tex` contient du texte issu du projet analyse. Meme echappe, le compiler
avec les droits de l'utilisateur revient a executer un programme sur des donnees
non fiables. Le conteneur applique les memes principes que le bac a sable :
sans reseau, utilisateur non root, repertoire limite, delai maximal.

Options obligatoires : `-interaction=nonstopmode`, `-halt-on-error`,
**`-no-shell-escape`**.

## Attention au livrable de la section 13.4

Le rapport genere atterrit dans `out/reports/`, qui est **ignore par Git**.
Le livrable exige devra etre copie ailleurs pour etre versionne, par exemple
`docs/example-report/evaluation.tex`. Sinon il ne sera jamais remis.
