# `security.sandbox` — Execution isolee

Repond a la section 8 : *« vous ne devez jamais executer directement sur votre
systeme hote un programme fourni par un autre groupe »*.

## Contenu

| Classe | Role |
|---|---|
| `SandboxRunner` | Contrat : `run(spec) -> SandboxResult` |
| `SandboxSpec` | Image, repertoire du projet, commande, limites |
| `SandboxLimits` | CPU, memoire, processus, delai, reseau, lecture seule, utilisateur |
| `SandboxResult` | Code de sortie, sortie standard, erreur, delai depasse |
| `DockerSandboxRunner` | Execution dans un conteneur dedie, supprime apres usage |
| `DisabledSandboxRunner` | **Implementation par defaut : refuse toute execution** |
| `SecurityPolicyViolationException` | Execution refusee |

## L'architecture imposee

```
Machine virtuelle dediee a l'experimentation
   +-- Conteneur Docker dedie, sans reseau
         +-- Projet evalue (code non fiable)
```

Les deux niveaux sont necessaires. Docker seul ne suffit pas : une evasion de
conteneur atteindrait la machine hote. La VM est la seconde barriere.

## Pourquoi `DisabledSandboxRunner` est l'implementation par defaut

`config/application.yaml` fixe `executionEnabled: false`. Executer le code d'un
autre groupe doit etre un **acte conscient**, jamais un comportement par defaut.
L'evaluation fonctionne sans : elle repose sur l'analyse statique et le modele.

## Les options `docker run` correspondant aux limites

```
--rm                       conteneur supprime apres usage
--network none             aucun acces reseau
--read-only                systeme de fichiers racine en lecture seule
--cap-drop ALL             aucune capacite privilegiee
--security-opt no-new-privileges
--user 1000:1000           utilisateur non root
--cpus / --memory / --pids-limit
-v <projet>:/workspace:ro  projet monte en LECTURE SEULE
```

Le delai maximal est applique cote Java, pas par Docker.

## Regle

`DockerSandboxRunner` doit appeler `ExecutionPolicy.verify(spec)` **avant** tout
lancement. Une specification sans limites ou en root est refusee, pas corrigee.
