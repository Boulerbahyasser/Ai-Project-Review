# Architecture

Ce document est la reference commune de l'equipe. Il correspond aux sections 9, 6,
10 et 8 du cahier des charges, et alimentera directement le rapport technique.

## 1. Decoupage en sous-systemes

Le decoupage reprend celui de la section 9 du cahier des charges, sans ajout :

| Sous-systeme | Responsabilite unique |
|---|---|
| `ui` | Afficher, saisir, restituer la progression |
| `application` | Orchestrer les cas d'utilisation |
| `project` | Importer et representer le projet a evaluer |
| `analysis` | Evaluer les criteres et consolider les scores |
| `llm` | Dialoguer avec les modeles de langage |
| `security` | Isoler l'execution, neutraliser le code non fiable |
| `report` | Construire et rendre le rapport |
| `persistence` | Conserver historique et cache |
| `configuration` | Fournir les parametres et les secrets |

## 2. Regle de dependance

```
                      +----+
                      | ui |
                      +----+
                         |
                         v
                  +-------------+
                  | application |
                  +-------------+
                   /     |     \      \
                  v      v      v      v
           +---------+ +--------+ +--------+ +-------------+
           | project | |analysis| | report | | persistence |
           +---------+ +--------+ +--------+ +-------------+
                          |
                          v
                      +-------+
                      |  llm  |
                      +-------+
                          |
                          v
                     +----------+
                     | security |
                     +----------+

configuration : lu au demarrage, injecte vers tous, aucune dependance sortante
```

Regles verifiees en revue de code :

1. Aucune fleche ne remonte. `analysis` n'importe jamais `ui`, `llm` n'importe jamais `analysis`.
2. `ui` ne parle qu'a la facade de `application`.
3. Un seul package effectue des appels reseau vers un modele : `llm`.
4. Les sous-systemes communiquent par **interfaces**, pas par implementations.
5. `configuration` ne depend de rien : il est injecte, jamais appele en retour.

## 3. Flux d'une analyse

```
1.  Choix d'une source par l'utilisateur      ui -> application
2.  Import et construction de l'arborescence  application -> project
3.  Choix du profil de criteres               ui -> application -> analysis
4.  Selection des fichiers a envoyer          project (inclusion / exclusion)
5a. Analyses deterministes                    analysis
5b. Analyses par modele                       analysis -> llm -> security
6.  Validation des reponses                   llm (parsing + validation)
7.  Consolidation des scores                  analysis (agregation)
8.  Construction du rapport                   application -> report
9.  Rendu LaTeX puis PDF optionnel            report (compilation isolee)
10. Enregistrement dans l'historique          application -> persistence

Progression et erreurs remontent en continu : application -> ui (evenements)
```

## 4. Design Patterns retenus

Le nombre de patterns n'est pas note ; leur pertinence l'est (section 6).
**Ne pas en ajouter sans probleme reel a resoudre.**

| Pattern | Ou | Probleme resolu |
|---|---|---|
| Facade | `application` | Donner a l'IHM un point d'entree simple sur le moteur |
| Composite | `project.model` | Traiter uniformement fichiers et repertoires |
| Strategy | `project.selection` | Changer les regles d'inclusion / exclusion de fichiers |
| Strategy | `llm.context` | Changer la maniere de decouper le code |
| Strategy | `analysis.aggregation` | Changer la regle de consolidation des scores |
| Strategy | `report.render` | Changer le format de sortie (LaTeX, HTML) |
| Abstract Factory | `llm` | Construire un fournisseur de modele selon la configuration |
| Factory | `project.loader`, `analysis.analyzer` | Choisir l'implementation selon la source / le critere |
| Template Method | `analysis.analyzer` | Fixer le deroulement d'une analyse, specialiser les etapes |
| Template Method | `llm.provider` | Ecrire le transport HTTP une fois pour tous les fournisseurs |
| Decorator | `analysis.analyzer.decorator` | Ajouter trace, tolerance aux pannes, cache autour d'un analyseur |
| Decorator | `llm.resilience`, `llm.cache` | Ajouter reprise, repli, cache autour d'un fournisseur |
| Builder | `llm.prompt`, `report.model` | Construire progressivement un prompt et un rapport |
| Observer | `application.event` | Notifier l'IHM sans que le moteur la connaisse |
| Command | `application.command` | Executer les operations hors du thread IHM |

## 5. Points d'extension (section 10)

Chaque ajout doit se faire **sans modifier les composants existants**.

| Ajouter... | Faire | Impact |
|---|---|---|
| un fournisseur de LLM | une implementation de `LlmProvider` + l'enregistrer dans la fabrique | 1 classe + 1 ligne |
| un critere d'evaluation | une entree dans `config/profiles/*.yaml` (+ un analyseur si deterministe) | 1 config (+1 classe) |
| un format de rapport | une implementation de `ReportRenderer` | 1 classe |
| une strategie de selection de fichiers | une implementation de `FileSelectionStrategy` | 1 classe |
| une methode d'analyse | un `Analyzer` + sa fabrique | 2 classes |
| une source d'import | un `ProjectLoader` + son enregistrement | 1 classe + 1 ligne |

Si un ajout impose de modifier de nombreuses classes, c'est un defaut d'architecture
a documenter dans `docs/DECISIONS.md` : le rapport doit l'expliquer (section 10).

## 6. Resilience aux erreurs du modele (section 5)

- `llm` : delai d'attente, reprise avec attente progressive, nombre maximal de
  tentatives, coupe-circuit, repli sur un fournisseur secondaire.
- `llm.parsing` : reponse vide, texte parasite autour du JSON, JSON mal forme,
  schema non respecte, score hors bornes -> erreur explicite, jamais de valeur devinee.
- `analysis` : l'echec d'un critere devient un resultat marque en echec ;
  l'analyse continue et le rapport signale le critere non evalue (recuperation partielle).
- Tout est trace (critere, modele, duree, nombre d'appels, erreurs), sans secret.

## 7. Architecture de securite (section 8)

```
Machine virtuelle dediee a l'experimentation
   +-- Conteneur Docker dedie, sans reseau
         +-- Projet evalue (code non fiable)
```

- Execution du projet analyse **desactivee par defaut** (`config/application.yaml`).
- Moindre privilege : utilisateur non root, systeme de fichiers restreint et en
  lecture seule quand possible, pas de reseau, limites CPU / memoire / processus,
  duree maximale, conteneur supprime apres usage.
- Import d'archive : rejet des chemins sortants (zip-slip).
- Injection de prompt : le contenu du projet est encadre par des delimiteurs et
  presente comme une **donnee a analyser** ; le prompt systeme l'interdit
  explicitement ; une consigne detectee dans le code est signalee comme faiblesse.
- Compilation LaTeX isolee, sans `--shell-escape`, sur du texte echappe.

## 8. Testabilite (section 11)

- Les interfaces sont les points de substitution : un faux fournisseur de LLM
  renvoie des reponses controlees (nominales, vides, mal formees, hors schema).
  **Aucun test n'appelle un vrai modele.**
- `analysis`, `project`, `report`, `llm.parsing` sont testables sans IHM.
- Projets d'exemple dans `src/test/resources/projects/`.
- `src/test/java` reproduit l'arborescence de `src/main/java`.

## 9. Choix techniques a justifier dans le rapport

| Choix | Raison |
|---|---|
| Java 21 | Version LTS ; types scelles et records pour un modele immuable |
| Maven | Standard, gestion des dependances, integration simple |
| JavaFX | Separation nette vue / logique, adapte a l'affichage d'un arbre |
| Client HTTP du JDK | Aucun SDK propriétaire : ne pas dependre d'un fournisseur |
| Jackson | Lecture des reponses JSON et persistance |
| Docker en ligne de commande | Isolation sans bibliotheque tierce |
| Fichiers JSON pour l'historique | Suffisant et lisible, sans serveur de base de donnees |
