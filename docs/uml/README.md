# Diagrammes UML

Diagrammes du sous-systeme LLM, etablis **a partir du code reel** de
`llm-subsystem/src/main/java/com/aireview/llm/` (module developpe par YSOT).

La section 13.3 du cahier des charges recommande d'inclure des diagrammes UML
« lorsqu'ils aident a mieux comprendre les choix effectues ».

| Fichier | Type UML | Ce qu'il montre |
|---|---|---|
| `01-classes-llm.md` | Diagramme de classes | Structure complete du sous-systeme |
| `02-sequence-evaluation.md` | Diagramme de sequence | Deroulement d'une evaluation, reessais inclus |
| `03-classes-patterns.md` | Diagramme de classes | Les patterns Adapter et Builder isoles |
| `04-etats-tentative.md` | Diagramme d'etats | Cycle de vie d'une tentative d'evaluation |

## Format

Les diagrammes sont ecrits en **Mermaid** : GitHub les affiche directement dans
le navigateur, ils se versionnent comme du code, et une correction ne demande
aucun outil.

## Export pour le rapport LaTeX

Trois voies, du plus simple au plus propre :

1. Afficher le fichier sur GitHub, capturer le diagramme, inclure le PNG.
2. `https://mermaid.live` : coller le source, exporter en SVG ou PNG.
3. `mmdc -i 01-classes-llm.md -o classes.pdf` (paquet `@mermaid-js/mermaid-cli`)
   puis `\includegraphics` — c'est la seule voie qui donne un rendu vectoriel.

## Regle de maintenance

Ces diagrammes decrivent le code, pas une intention. **Si une signature change,
le diagramme doit changer dans le meme commit.** Un diagramme qui promet plus que
le code est reperable en soutenance et jette le doute sur le reste.
