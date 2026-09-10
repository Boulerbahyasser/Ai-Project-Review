# `llm.provider` — Les fournisseurs concrets

Une classe par fournisseur de modele. Chacune joue **deux patterns a la fois** :

- **Adapter** : elle traduit notre `LlmRequest` vers le format du fournisseur, et
  la reponse du fournisseur vers notre `LlmResponse`.
- **Template Method** : la sequence HTTP est ecrite une seule fois dans
  `AbstractHttpLlmProvider` ; chaque sous-classe ne specialise que le format.

## Contenu

| Classe | Role |
|---|---|
| `AbstractHttpLlmProvider` | `ask()` est `final` : corps de requete -> envoi -> extraction -> latence |
| `LocalOpenAiCompatibleProvider` | Modele local (LM Studio / Ollama). **Fournisseur par defaut** |
| `MistralProvider` | Mistral, cle lue dans l'environnement |
| `DeepSeekProvider` | DeepSeek, illustre l'ajout d'un fournisseur |
| `DefaultLlmProviderFactory` | Assemble transport + reessais + cache |

## Les methodes a ecrire pour un nouveau fournisseur

```java
protected String     endpoint();                        // son URL
protected ObjectNode requestBody(LlmRequest request);   // notre objet -> leur JSON
protected String     extractContent(JsonNode response); // leur JSON   -> notre texte
```

Deux points de conception a connaitre :

- `requestBody` retourne un **arbre Jackson**, pas une chaine. C'est volontaire :
  les prompts contiennent du code source, donc des guillemets, des antislashs et
  des retours a la ligne. Un corps assemble par concatenation serait du JSON
  invalide des le premier fichier Java analyse. Le type de retour rend l'erreur
  impossible.
- Ces methodes sont **pures** : elles se testent sans reseau, avec une reponse
  JSON copiee de la documentation du fournisseur. C'est la reponse a la question 6
  de la section 19.

Deux points optionnels, a redefinir si le fournisseur les renseigne :
`extractUsage` (jetons consommes) et `servedModel` (modele reellement servi).

## Le format compatible OpenAI est mutualise

`OpenAiChatFormat` porte la traduction du format « chat completions » : Mistral,
DeepSeek, LM Studio et l'interface compatible d'Ollama le partagent. Les trois
fournisseurs actuels s'y delegent entierement et ne different que par leur URL et
leur authentification.

Consequence pratique : n'ecrivez une nouvelle traduction que si le fournisseur
utilise reellement un autre format.

## Ajouter un fournisseur (section 10)

1. Une classe qui etend `AbstractHttpLlmProvider` et implemente les deux methodes.
2. Une fabrique qui repond `supports("son-id")`.
3. Une ligne dans la liste d'assemblage. **Aucun fichier existant modifie.**

## Regles absolues

- Aucune cle d'API en dur. Elle vient de `configuration` via `SecretProvider`,
  qui la lit dans une variable d'environnement (section 17).
- Aucun detail propre a un fournisseur ne doit remonter dans `LlmResponse` : un
  champ que seul Mistral possede signifierait que l'Adapter a echoue. Traduisez
  ces specificites en concepts universels, par exemple une exception.
