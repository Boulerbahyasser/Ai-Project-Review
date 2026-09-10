# `src/test/java` — Tests

Repond a la section 11 du cahier des charges. Exigence en gras :
**l'architecture doit permettre de tester le logiciel sans appeler reellement un
LLM a chaque test**.

## Arborescence

Miroir de `src/main/java`. Un test se trouve dans le meme package que la classe
testee, ce qui donne acces aux membres de portee package.

| Repertoire | Contenu attendu |
|---|---|
| `fixtures/` | Doublures et donnees de test partagees — **a ecrire en premier** |
| `project/` | Import, classification, selection de fichiers |
| `analysis/` | Analyseurs, agregation, decorateurs |
| `llm/` | Parsing, validation, resilience, construction des prompts |
| `report/` | Assemblage, echappement LaTeX, rendu |
| `security/` | Politique d'execution, anti-injection, masquage des secrets |

## Les sept points a tester (section 11)

1. Le chargement d'un projet
2. Les analyseurs
3. La generation de rapport
4. Le parsing des reponses du modele
5. Le traitement des reponses invalides
6. Les strategies de recuperation d'erreur
7. Les composants **independamment de l'IHM**

## La premiere classe a ecrire

`fixtures/FakeLlmProvider` — une implementation de `LlmProvider` qui renvoie des
reponses preparees ou leve des exceptions choisies. Elle satisfait a elle seule
l'exigence en gras, et debloque les tests de `analysis`, `report` et `application`.

## Ce qu'aucun test ne doit faire

- Appeler un vrai modele de langage.
- Lancer un conteneur Docker.
- Ecrire hors du repertoire temporaire fourni par JUnit (`@TempDir`).
- Dependre de l'ordre d'execution des autres tests.

## Outils disponibles

JUnit 5, Mockito et AssertJ sont declares dans le `pom.xml`. Preferez une
doublure ecrite a la main (`FakeLlmProvider`) a un mock Mockito quand le
comportement attendu est une sequence de reponses : c'est plus lisible et ca
documente les cas d'erreur.
