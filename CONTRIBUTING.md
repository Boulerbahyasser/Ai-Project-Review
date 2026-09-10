# Regles de contribution

## Branches

- `main` : toujours compilable (`mvn verify` passe). Aucun commit direct.
- `feat/<lot>-<sujet>` : nouvelle fonctionnalite (ex. `feat/lot-c-mistral-provider`).
- `fix/<sujet>` : correction.

## Commits

Format court et explicite, en anglais ou en francais, mais **un seul style** :

```
llm: ajoute le fournisseur Mistral
analysis: corrige l'agregation des criteres en echec
```

Un commit = une intention. Pas de commit "wip" sur `main`.

## Pull requests

1. Une issue -> une branche -> une pull request.
2. Relecture par au moins un autre membre avant fusion.
3. La PR decrit **ce qui change** et **pourquoi**, et cite l'issue.

## Points verifies en revue

- [ ] `mvn verify` passe.
- [ ] Respect des regles de dependance de `docs/ARCHITECTURE.md`
      (aucune fleche remontante, aucun appel LLM hors du package `llm`).
- [ ] Aucune logique metier dans `ui`.
- [ ] Aucune cle d'API, aucun secret, aucun jeton dans le code ou la configuration.
- [ ] Les erreurs sont traitees : pas de `catch` vide, pas d'exception avalee.
- [ ] Les nouveaux composants sont testables sans appel reel a un modele.
- [ ] Un Design Pattern introduit resout un probleme reel et est note dans
      `docs/DECISIONS.md` (un pattern ajoute artificiellement est sanctionne).
- [ ] Toute decision d'architecture est consignee dans `docs/DECISIONS.md`.

## Style de code

- Java 21. Indentation 4 espaces, pas de tabulation.
- Une classe publique par fichier ; le nom du fichier est celui de la classe.
- Champs `final` par defaut ; `record` pour les porteurs de donnees.
- Dependances passees par le constructeur (pas de singleton statique).
- Pas de `System.out.println` : utiliser le journal (SLF4J).

## Avant de coder

Lisez `docs/ARCHITECTURE.md`, puis le `README.md` du package concerne : il precise
ce qui doit y aller et ce qui y est interdit.
