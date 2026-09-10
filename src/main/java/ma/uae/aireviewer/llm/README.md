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

---

## Implementation apportee par la branche `llm-nearly-done`

Une implementation complete et testee du sous-systeme a ete developpee en
parallele dans un module autonome, sous le package `com.aireview.llm` :
fournisseur Ollama, construction de prompts, decoupage de contexte, evaluateur
resilient, doublure de test, et environ 2700 lignes de tests.

Elle se trouve dans `llm-subsystem/` a la racine du depot, avec sa propre
documentation (`ARCHITECTURE.md`, `Tasks.md`).

**Decision d'integration en attente** : ce module et le present package couvrent
le meme perimetre avec des contrats differents (`LLMProvider.call()` d'un cote,
`LlmProvider.ask()` de l'autre). L'un des deux doit devenir la reference, et le
choix doit etre consigne dans `docs/DECISIONS.md`.
