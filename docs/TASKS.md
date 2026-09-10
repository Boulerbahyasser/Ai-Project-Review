# Repartition du travail

Groupe de 5. Le cahier des charges demande d'expliquer la repartition des taches
dans le rapport (section 13.3, point 6) : tenez ce fichier a jour.

## Lots

| Lot | Perimetre | Packages | Livrable |
|---|---|---|---|
| A | Import et representation du projet | `project` | Import repertoire / archive / Git, classification, selection de fichiers |
| B | Moteur d'evaluation | `analysis` | Criteres, profils, analyseurs deterministes, decorateurs, agregation |
| C | Integration des modeles | `llm` | Fournisseurs, resilience, prompts, parsing / validation, gestion du contexte |
| D | Rapport et persistance | `report`, `persistence` | Modele de rapport, rendu LaTeX, compilation PDF, historique, cache |
| E | IHM, orchestration, securite | `ui`, `application`, `security` | Fenetre principale, facade, evenements, bac a sable Docker, anti-injection |

`configuration` est partage : chacun ajoute ce dont il a besoin, avec revue croisee.

## Ordre de demarrage conseille

1. **Ensemble** : valider `docs/ARCHITECTURE.md` et figer les interfaces publiques
   de chaque sous-systeme. Aucune interface ne change ensuite sans accord du groupe.
2. Lot A et lot C peuvent avancer en parallele des le depart.
3. Lot B demarre avec un faux fournisseur de LLM, sans attendre le lot C.
4. Lot D demarre avec des resultats d'analyse fabriques a la main.
5. Lot E integre l'ensemble une fois les facades disponibles.

Le faux fournisseur de LLM (`src/test/java/.../fixtures`) est donc **la premiere
chose a ecrire** : il debloque les lots B, D et E.

## Regle anti-blocage

Personne n'attend le code d'un autre : on programme contre les interfaces et on
substitue une implementation de test. Si une interface manque, on la cree et on
le signale dans le canal de l'equipe.

## Suivi

Creez une issue GitHub par element du tableau ci-dessus, avec l'etiquette du lot
(`lot-a` ... `lot-e`). Une issue = une branche = une pull request.

## Elements a ne pas oublier (notes dans l'evaluation)

- Tests de chaque composant, sans appel reel a un modele (section 11).
- Rapport `evaluation.tex` genere automatiquement + son PDF si possible (section 13.4).
- README permettant a un tiers de compiler, configurer, lancer et analyser (section 13.2).
- Rapport technique repondant aux 9 questions de la section 19.
