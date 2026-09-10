# `src/main/resources` — Ressources de l'application

Contenu embarque dans le jar et charge depuis le classpath.

| Chemin | Contenu | Charge par |
|---|---|---|
| `prompts/` | Gabarits de prompts | `llm.prompt.ClasspathPromptTemplateRepository` |
| `templates/` | Gabarit LaTeX du rapport | `report.latex.LatexTemplate` |
| `logback.xml` | Configuration de journalisation | Logback, au demarrage |

## Ressources ici, configuration dans `config/`

| | `src/main/resources/` | `config/` |
|---|---|---|
| Nature | Embarque dans le jar | Fichier externe, modifiable |
| Modifie par | Les developpeurs | L'utilisateur |
| Exemples | Prompts, gabarit LaTeX | URL du modele, criteres, limites Docker |

Un prompt est du **contenu de l'application**, pas un reglage : il est versionne
avec le code. L'URL d'un serveur est un **reglage** : elle vit dans `config/`.

## Regle absolue

Aucun secret dans ce repertoire. Il est embarque dans le jar, donc distribuable
(section 17 : *« aucun secret ni jeton d'API ne doit etre present dans le depot »*).
