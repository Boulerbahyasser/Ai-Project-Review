# `security.secret` — Masquage des secrets

Repond a la fin de la section 12 : la trace et les journaux ne doivent
*« pas enregistrer accidentellement »* de cles d'API, mots de passe, secrets ou
donnees confidentielles.

## Contenu

| Classe | Role |
|---|---|
| `SecretRedactor` | Contrat : `redact(texte) -> texte masque` |
| `PatternSecretRedactor` | Masquage par motifs connus |

## Ce qu'il faut masquer

| Motif | Exemple |
|---|---|
| En-tetes d'autorisation | `Authorization: Bearer sk-...` |
| Cles de fournisseurs | prefixes `sk-`, `ghp_`, `hf_`, `gsk_` |
| Champs nommes | `apiKey`, `api_key`, `token`, `password`, `secret` dans du JSON |
| Chaines de connexion | `postgres://user:motdepasse@...` |

## Ou l'appliquer

| Point | Pourquoi |
|---|---|
| Avant tout `TraceEvent` | La trace est persistee avec le resultat d'analyse |
| Avant toute journalisation d'une requete ou reponse HTTP | Les en-tetes contiennent la cle |
| Avant tout message d'exception remonte a l'IHM | Une erreur HTTP peut renvoyer l'URL complete |

Le troisieme cas est le plus insidieux : un message d'erreur affiche a l'ecran ou
copie dans une capture pour le rapport peut exposer une cle.

## Regle

Le masquage remplace par un marqueur explicite (`***`), il ne supprime pas la
ligne : on doit toujours pouvoir diagnostiquer, sans lire le secret.
