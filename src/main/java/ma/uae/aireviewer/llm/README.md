# `llm` - Communication avec les modeles de langage

Reference : cahier des charges sections 4 et 5. Exigences : abstraction derriere une
interface, remplacement d'un fournisseur sans impact, resilience aux erreurs.

## Contenu attendu

- Interface du fournisseur (`ask(requete) -> reponse`) + types de requete / reponse
  + hierarchie d'exceptions (delai depasse, service indisponible, reponse invalide).
- Fabrique de fournisseurs (pattern Abstract Factory), pilotee par la configuration.
- `provider/` : une implementation par fournisseur (modele local expose en HTTP,
  Mistral, DeepSeek...). Mutualiser le transport HTTP dans une classe abstraite
  (pattern Template Method) : chaque fournisseur ne specialise que le format.
- `resilience/` : reprise sur erreur avec attente progressive, repli sur un
  fournisseur secondaire, coupe-circuit (pattern Decorator).
- `cache/` : evitement des appels identiques (section 4.2).
- `prompt/` : construction de prompts structures (role, critere, elements fournis,
  format attendu) a partir de gabarits de `src/main/resources/prompts/` - pattern Builder.
- `parsing/` : lecture et **validation** de la reponse JSON avant toute utilisation.
- `context/` : strategie de gestion du contexte (selection, decoupage, resumes
  intermediaires, agregation) - section 4.2.

## Regles absolues

- **Aucun appel HTTP vers un LLM ailleurs que dans ce package** (section 16.2 :
  les appels disperses sont sanctionnes).
- Aucune cle d'API dans le code ni dans le depot : elles proviennent de `configuration`,
  qui les lit dans les variables d'environnement.
- Le contenu du projet analyse traverse obligatoirement la protection
  anti-injection de `security` avant d'entrer dans un prompt.
- Toute reponse est validee (JSON conforme, score dans les bornes) avant conversion
  en resultat metier.
