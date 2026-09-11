# `security.policy` — Verification du moindre privilege

Garde-fou applique avant tout lancement de code non fiable. La section 8.1
enumere douze restrictions ; ce package verifie qu'elles sont respectees.

## Contenu

| Classe | Role |
|---|---|
| `ExecutionPolicy` | Contrat : `verify(spec)`, leve une exception si non conforme |
| `LeastPrivilegePolicy` | Refuse root, reseau actif, absence de limites ou de delai |

## Ce qui doit etre refuse

| Condition | Raison |
|---|---|
| `user` vaut `root` ou `0:0` | Section 8.1 : utilisateur non root obligatoire |
| `networkDisabled == false` | Section 8.1 : pas d'acces reseau par defaut |
| `timeout` nul ou absent | Un conteneur sans delai peut tourner indefiniment |
| Limites CPU / memoire / processus absentes | Une bombe de fork sature la machine |
| Repertoire monte en ecriture | Le projet analyse pourrait modifier vos fichiers |
| Chemin monte hors du repertoire du projet | Acces aux fichiers personnels |

## Pourquoi une classe separee du runner

Deux raisons defendables en soutenance :

1. **Testabilite** : la politique se teste sans Docker, en lui soumettant des
   specifications volontairement dangereuses et en verifiant le refus.
2. **Verrou unique** : toute implementation future de `SandboxRunner` passe par
   la meme verification. Impossible d'ajouter un runner qui l'oublie.

## Regle

Une politique **refuse**, elle ne **corrige** pas. Ajuster silencieusement une
specification dangereuse masquerait une erreur de conception ailleurs.
