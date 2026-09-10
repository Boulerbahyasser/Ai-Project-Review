# `llm.prompt` — Construction des prompts

Repond a la section 4.3 : les prompts doivent etre **structures** et specifier le
role du modele, le critere evalue, les elements fournis et le format attendu.

## Contenu

| Classe | Role |
|---|---|
| `PromptSection` | Fragment de prompt, avec un drapeau `untrusted` |
| `PromptTemplate` | Gabarit charge des ressources, substitution `{{cle}}` |
| `PromptTemplateRepository` | Contrat d'acces aux gabarits |
| `ClasspathPromptTemplateRepository` | Lecture depuis `/prompts/*.txt` |
| `PromptBuilder` | **Builder** : assemble role + sections + format attendu |
| `CriterionPromptFactory` | Produit le `LlmRequest` d'un critere |
| `DefaultCriterionPromptFactory` | Implementation fondee sur les gabarits |

## Le point de securite le plus important du projet

`PromptSection.untrusted(...)` marque le contenu venant du projet analyse.
`PromptBuilder.build()` fait passer **obligatoirement** ce contenu par
`security.prompt.PromptInjectionGuard`.

Consequence : la protection contre l'injection de prompt (section 8.2) n'est pas
une consigne qu'un developpeur peut oublier — elle est imposee par la structure
du code. Tout contenu du projet analyse entrant dans un prompt sans passer par
`untrusted()` est un **bug de securite**, pas un ecart de style.

## Pourquoi les prompts vivent dans les ressources

`src/main/resources/prompts/` contient les gabarits, versionnes avec le code.
Un prompt s'ajuste par essais successifs : le sortir du Java permet de le
modifier sans recompiler et de suivre son historique dans Git.

## Ne va pas ici

- L'appel HTTP : il est dans `llm.provider`.
- La lecture de la reponse : elle est dans `llm.parsing`.
