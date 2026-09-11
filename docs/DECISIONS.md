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

## 005 - Isolation Docker sans machine virtuelle dediee

**Contexte.** Section 8 : l'architecture recommandee est VM -> Docker -> projet
evalue. L'equipe developpe sur des postes personnels avec Docker Desktop, sans
machine virtuelle dediee a l'experimentation disponible pour la duree du projet.

**Decision.** Isoler uniquement au niveau Docker (conteneurs `sandbox`, `unzip`,
`latex`, cf. `docs/rapport/securite-docker.md`), sans second niveau de VM, et
documenter cette limite explicitement plutot que de la laisser implicite.

**Alternatives ecartees.** Une VM locale (VirtualBox/Hyper-V) par poste : cout
de configuration et de maintenance disproportionne pour un projet academique,
et incompatible avec l'exigence de la section 13.2 (« un tiers doit pouvoir
compiler, configurer et lancer » facilement).

**Consequences.** Le noyau reste partage entre les conteneurs et la machine
hote : une evasion de conteneur atteindrait directement l'hote, ce qu'une VM
aurait empeche. Risque residuel assume pour un projet academique, documente
dans `docs/rapport/securite-docker.md` (section limites) plutot que dans le code.

---

## 006 - Extraction d'archive isolee au moment de l'import

**Contexte.** `ArchiveProjectLoader` decompressait les archives `.zip` avec
`java.util.zip.ZipInputStream` directement dans le processus de l'application,
avec uniquement une verification de zip-slip locale — avant que le package
`security.archive` n'existe.

**Decision.** Deleguer l'extraction a `ArchiveExtractor` (`security.archive`),
qui s'execute dans un conteneur Docker dedie (`ai-reviewer/unzip`) : sans
reseau, sortie extraite bornee en taille et en nombre d'entrees, noms de
fichiers dangereux et liens symboliques rejetes avant toute ecriture sur l'hote.

**Alternatives ecartees.** Renforcer le `ZipInputStream` existant (limites de
taille/nombre d'entrees verifiees en Java, sans conteneur) : ecarte, car la
decompression continuerait de s'executer avec les droits du processus hote —
ce que la section 8 interdit pour le contenu fourni par un autre groupe.

**Consequences.** `project.loader` depend desormais de `security.archive` (une
seule interface, un seul point d'appel, injectee au constructeur) — une arete
absente du diagramme initial de `docs/ARCHITECTURE.md`, a faire valider par
l'equipe. Sans Docker disponible, l'import d'archive echoue explicitement
plutot que de se rabattre sur une extraction non isolee.

---

## 007 - Compilation LaTeX dans un conteneur durci

**Contexte.** Le `.tex` genere par `report.render` contient de la prose produite
par le LLM a partir du code source du projet evalue : son contenu est non
fiable par transitivite, meme si le fichier est produit par notre propre
application (section 8.2).

**Decision.** Compiler dans un conteneur dedie (`ai-reviewer/latex`), aligne sur
les memes principes que `DockerSandboxRunner` : lecture seule, `--cap-drop ALL`,
`--security-opt no-new-privileges`, utilisateur non root, memoire et nombre de
processus limites, `-no-shell-escape` impose cote Java en plus de l'image,
sortie ecrite dans un repertoire jetable plutot que dans le repertoire du
rapport.

**Alternatives ecartees.** Compiler avec les droits de l'utilisateur courant en
se fiant uniquement a `-no-shell-escape` dans l'`ENTRYPOINT` de l'image : ecarte,
une seule option mal propagee (image reconstruite, appel direct a `pdflatex`)
suffirait a rouvrir `\write18`.

**Consequences.** Le journal de compilation est capture et les secrets y sont
masques (`SecretRedactor`) avant d'etre ecrits sur disque, pour permettre le
diagnostic d'un echec sans exposer une cle qui aurait fuite dans la prose
generee.
