# Journal des decisions d'architecture

Une entree par decision structurante. Ce journal alimente directement le rapport
technique (sections 13.3 et 19 du cahier des charges).

Format : contexte, decision, alternatives ecartees, consequences.

---

## 001 - Decoupage en sous-systemes de la section 9

**Contexte.** Le cahier des charges propose un decoupage (`ui`, `application`,
`project`, `analysis`, `llm`, `security`, `report`, `persistence`, `configuration`)
et autorise une autre organisation si elle est justifiee.

**Decision.** Reprendre ce decoupage a l'identique, sans package transverse.

**Alternatives ecartees.** Un package utilitaire commun : il devient vite un
fourre-tout et cree des dependances croisees entre sous-systemes.

**Consequences.** Chaque sous-systeme porte ses propres exceptions. La trace
d'analyse appartient a `analysis`, son stockage a `persistence`.

---

## 002 - Acces aux modeles derriere une seule interface

**Contexte.** Section 4.1 : il faut pouvoir remplacer un fournisseur. Section 16.2 :
les appels HTTP disperses sont sanctionnes.

**Decision.** Une interface unique dans `llm`, des implementations par fournisseur,
une fabrique pilotee par la configuration. Resilience et cache ajoutes par decoration.

**Consequences.** Changer de modele = changer une ligne de configuration.
Aucun autre package ne connait le protocole d'un fournisseur.

---

## 003 - Modele local par defaut

**Contexte.** Le projet analyse peut etre confidentiel ; le cahier des charges
recommande de supporter un modele local (section 4).

**Decision.** `providerId: local` par defaut, vers une API compatible OpenAI
(LM Studio / Ollama). Aucune cle requise, aucune donnee sortante.

---

## 004 - Execution du projet analyse desactivee par defaut

**Contexte.** Section 8 : le projet analyse est potentiellement malveillant.

**Decision.** L'implementation par defaut du bac a sable **refuse** toute execution.
L'execution Docker doit etre activee explicitement en configuration, et seulement
dans une machine virtuelle dediee.

**Consequences.** L'evaluation repose sur l'analyse statique et le LLM ; l'execution
reste une capacite optionnelle et encadree.

---

## 005 - <a completer par l'equipe>
