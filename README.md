# AI Project Reviewer

Plateforme d'evaluation automatisee de projets logiciels, combinant analyses
deterministes et modeles de langage, avec generation d'un rapport LaTeX.

> Projet de Master Informatique - Architecture logicielle et IA.
> Cahier des charges : `sujet2627-m1-en.pdf` (a la racine).

**Etat du depot : squelette d'architecture.** Les repertoires et les contrats sont
poses, l'implementation reste a ecrire. Chaque package contient un `README.md` qui
precise ce qui doit y aller et ce qui y est interdit. Lisez d'abord
`docs/ARCHITECTURE.md`, puis le README du package sur lequel vous travaillez.

## Sommaire

| Document | Contenu |
|---|---|
| `docs/ARCHITECTURE.md` | Architecture, regles de dependance, Design Patterns, points d'extension |
| `docs/TASKS.md` | Repartition du travail entre les 5 membres et ordre de demarrage |
| `docs/DECISIONS.md` | Journal des decisions d'architecture (a alimenter pour le rapport) |
| `CONTRIBUTING.md` | Branches, commits, revue de code, regles de qualite |
| `src/main/java/ma/uae/aireviewer/README.md` | Role de chaque sous-systeme |

## Prerequis

- JDK 21 ou superieur
- Maven 3.9+
- Docker (isolation d'execution et compilation LaTeX)
- Un modele de langage accessible : serveur local (LM Studio / Ollama) ou cle d'API

## Demarrage

```bash
git clone <url-du-depot>
cd ai-project-reviewer
mvn -q verify          # compile et lance les tests
mvn javafx:run         # lance l'interface graphique
```

## Configuration

Toute la configuration est dans `config/application.yaml`.
**Aucune cle d'API dans le depot** : le fichier ne contient que le nom de la
variable d'environnement a lire.

```bash
export LLM_API_KEY="votre-cle"   # PowerShell : $env:LLM_API_KEY="votre-cle"
```

Par defaut, l'application vise un modele local (`providerId: local`), donc aucune
cle n'est necessaire et aucun code du projet analyse ne quitte la machine.

## Images Docker

Trois images sont necessaires (section 8) : le bac a sable d'execution, la
compilation LaTeX et l'extraction d'archives.

| Image | Dockerfile | Role |
|---|---|---|
| `ai-reviewer/sandbox:latest` | `docker/sandbox/Dockerfile` | Execution isolee du projet analyse (desactivee par defaut) |
| `ai-reviewer/latex:latest` | `docker/latex/Dockerfile` | Compilation `.tex` -> `.pdf`, sans `--shell-escape` |
| `ai-reviewer/unzip:latest` | `docker/unzip/Dockerfile` | Extraction isolee des archives `.zip` importees |

Construction (les noms viennent de `config/application.yaml` : ne pas les
changer d'un cote sans changer l'autre) :

```bash
./scripts/build-images.sh        # Linux / macOS / Git Bash
```

```powershell
.\scripts\build-images.ps1       # Windows PowerShell
```

Si Docker n'est pas installe ou n'est pas lance, le script s'arrete avec un
message clair avant de tenter la moindre construction. L'application
elle-meme ne l'exige pas au demarrage : `DisabledSandboxRunner`,
`DisabledArchiveExtractor` et `NoOpPdfCompiler` prennent le relais sans faire
echouer une analyse.

## Structure du depot

```
ai-project-reviewer/
|-- pom.xml                     Build Maven, dependances, plugins
|-- README.md
|-- CONTRIBUTING.md
|-- config/
|   |-- application.yaml         Parametres (LLM, analyse, sandbox, rapport)
|   `-- profiles/                Profils de criteres d'evaluation
|-- docker/
|   |-- sandbox/Dockerfile       Conteneur d'execution du projet analyse
|   `-- latex/Dockerfile         Conteneur de compilation LaTeX
|-- docs/
|   |-- ARCHITECTURE.md
|   |-- TASKS.md
|   `-- DECISIONS.md
|-- scripts/                     Scripts utilitaires (build image, lancement)
|-- out/                         Sorties generees (rapports, historique, cache)
|-- src/main/java/ma/uae/aireviewer/
|   |-- ui/                      Interface graphique
|   |-- application/             Cas d'utilisation, facade, evenements
|   |-- project/                 Import et representation du projet analyse
|   |-- analysis/                Moteur d'evaluation, criteres, resultats
|   |-- llm/                     Communication avec les modeles de langage
|   |-- security/                Bac a sable Docker, anti-injection
|   |-- report/                  Construction et rendu LaTeX
|   |-- persistence/             Historique, cache
|   `-- configuration/           Parametres, secrets
|-- src/main/resources/
|   |-- prompts/                 Gabarits de prompts (versionnes)
|   |-- templates/               Gabarit LaTeX du rapport
|   `-- logback.xml              Journalisation
`-- src/test/java/ma/uae/aireviewer/
    `-- ...                      Tests, miroir de la structure principale
```

Les sous-systemes sont ceux imposes par la section 9 du cahier des charges.

## Regle a retenir avant de coder

- L'IHM ne contient aucune logique metier.
- Aucun appel a un modele de langage en dehors du package `llm`.
- Aucune cle d'API dans le depot.
- Le projet analyse est du code **non fiable** : jamais execute sur la machine hote.
