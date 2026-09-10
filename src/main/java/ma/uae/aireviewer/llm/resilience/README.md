# `llm.resilience` — Encaisser les pannes

Repond a la section 5 : *« un LLM n'est pas un composant deterministe »*.
Toutes ces classes sont des **Decorator** de `LlmProvider` : elles s'ajoutent
autour du transport sans que l'appelant le sache.

## Contenu

| Classe | Role |
|---|---|
| `RetryPolicy` | Nombre maximal de tentatives, attente initiale, multiplicateur |
| `RetryingLlmProvider` | Rejoue les echecs transitoires avec attente progressive |
| `FallbackLlmProvider` | Bascule sur un fournisseur secondaire |
| `CircuitBreakerLlmProvider` | Cesse d'appeler apres N echecs consecutifs |

## Quelles erreurs rejouer, et lesquelles surtout pas

| Erreur | Rejouable | Pourquoi |
|---|---|---|
| `LlmTimeoutException` | oui | le serveur peut repondre au prochain essai |
| `LlmUnavailableException` (5xx) | oui | panne temporaire |
| HTTP 429 (trop de requetes) | oui | avec une attente plus longue |
| `LlmInvalidResponseException` | une fois | avec une consigne de reformatage |
| HTTP 401 / 403 (cle invalide) | **non** | rejouer ne changera rien : echec immediat |

Rejouer une erreur non rejouable fait perdre du temps et masque la cause reelle.

## Pourquoi `FallbackLlmProvider` compte pour la note

La section 16.2 sanctionne *« une dependance forte a un seul modele de langage »*.
Cette classe y repond directement : modele principal indisponible, l'analyse
continue sur un second.

## Le coupe-circuit, cas concret

Serveur local arrete. Sans coupe-circuit, chacun des 12 criteres tente 3 appels
avec attente progressive : l'analyse met plusieurs minutes a echouer. Avec, apres
3 echecs consecutifs le circuit s'ouvre et les criteres restants echouent
immediatement.

## Regle

Ces decorateurs ne connaissent **aucun** fournisseur en particulier. Ils
enveloppent un `LlmProvider`, quel qu'il soit.
