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

## Les deux seules methodes a ecrire pour un nouveau fournisseur

```java
protected String requestBody(LlmRequest request);       // notre objet -> leur JSON
protected String extractContent(String responseBody);   // leur JSON   -> notre texte
```

Ce sont des fonctions **pures** `String -> String` : elles se testent sans reseau,
avec une reponse JSON copiee de la documentation du fournisseur. C'est la reponse
a la question 6 de la section 19.

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
