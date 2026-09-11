# Racine du code : `ma.uae.aireviewer`

Les sous-repertoires correspondent **exactement** aux sous-systemes imposes par la
section 9 du cahier des charges. N'en ajoutez pas sans validation de l'equipe et
sans justification dans `docs/ARCHITECTURE.md`.

| Package | Responsabilite | Ne doit JAMAIS contenir |
|---|---|---|
| `ui` | Interface graphique (JavaFX) | Logique metier, appel HTTP, lecture de fichier projet |
| `application` | Cas d'utilisation, orchestration, facade, evenements | Code JavaFX, code HTTP, LaTeX |
| `project` | Import et representation du projet analyse | Appel LLM, generation de rapport |
| `analysis` | Moteur d'evaluation, criteres, resultats | Appel HTTP direct, code JavaFX |
| `llm` | Communication avec les modeles de langage | Regles de notation, LaTeX |
| `security` | Bac a sable Docker, defense anti-injection | IHM, notation |
| `report` | Construction et rendu du rapport (LaTeX) | Appel LLM, IHM |
| `persistence` | Historique des analyses, cache | IHM, appel LLM |
| `configuration` | Parametres de l'application, acces aux secrets | Tout le reste |

## Regle de dependance

Le sens des dependances autorisees est decrit dans `docs/ARCHITECTURE.md`.
Regle courte : `ui` -> `application` -> (`project`, `analysis`, `report`, `persistence`),
`analysis` -> `llm` -> `security`. Aucune fleche ne remonte : `analysis` ne connait
pas `ui`, `llm` ne connait pas `analysis`.

## Une classe = un fichier, un package = une responsabilite

Avant de creer une classe, verifiez le tableau ci-dessus et le README du
sous-repertoire concerne.
