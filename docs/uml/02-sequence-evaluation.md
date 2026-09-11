# Diagramme de sequence — evaluation d'un projet

Deroulement reel de `ResilientEvaluator.evaluate(projectName, sourceCode, model)`,
reessais compris.

```mermaid
sequenceDiagram
    autonumber
    actor Appelant
    participant CE as ChunkedEvaluator
    participant CS as ContextSplitter
    participant RE as ResilientEvaluator
    participant PB as PromptBuilder
    participant P as LLMProvider
    participant O as OllamaProvider

    Appelant->>CE: evaluate(projet, code, modele)

    Note over CE,CS: Etage 1 — gestion du contexte
    CE->>CS: split(code)
    alt le code tient dans une requete
        CS-->>CE: [1 morceau]
    else code trop volumineux
        CS-->>CE: [n morceaux avec recouvrement]
    end

    loop pour chaque morceau
        CE->>RE: evaluate(projet, morceau, modele)

        loop pour chacun des 3 criteres
            Note over RE,PB: Etage 2 — assemblage du prompt

            RE->>PB: forCriterion(critere)
            RE->>PB: withSourceCode(code)
            RE->>PB: withProjectName(projet)
            RE->>PB: build()
            PB->>PB: sanitiseSourceCode(code)
            Note right of PB: neutralise les delimiteurs<br/>presents dans le code (section 8.2)
            PB-->>RE: prompt = role + exemple few-shot + charge utile

            Note over RE,O: Etage 3 — appel au modele
            RE->>P: call(LLMRequest)
            P->>O: call(...)
            O->>O: POST /api/generate
            O-->>P: LLMResponse(rawContent, modele, duree)
            P-->>RE: LLMResponse

            Note over RE: Etage 4 — validation
            RE->>RE: extractJson(rawContent)
            RE->>RE: parse vers CriterionResult
            RE->>RE: validation semantique

            alt reponse valide
                RE-->>RE: CriterionResult conserve
            else echec — transport, JSON ou schema
                RE->>RE: applyBackoff(tentative)
                Note right of RE: attente = 500 ms x 2^(n-1)
                RE->>P: call(LLMRequest)
                Note right of RE: LIMITE : le prompt est reconstruit<br/>a l'identique, l'erreur n'y est pas ajoutee
            end
        end

        RE-->>CE: EvaluationResult du morceau
    end

    opt plusieurs morceaux
        CE->>CS: aggregateChunkResults(resultats)
        Note right of CS: moyenne des scores,<br/>commentaires joints,<br/>problemes dedoublonnes
        CS-->>CE: CriterionResult agrege
    end

    CE-->>Appelant: EvaluationResult
```

## Ce que ce diagramme rend visible

**Le nombre d'appels au modele.** Trois criteres multiplies par le nombre de
morceaux : un projet decoupe en dix morceaux declenche trente appels. C'est le
probleme de cout que la section 4.2 demande de traiter (*« eviter les appels
inutiles autant que possible »*), et aucun cache n'intervient ici.

**Le point de neutralisation.** L'appel `sanitiseSourceCode` a lieu dans
`build()`, donc **avant** toute sortie vers le reseau. Aucun chemin ne permet a du
code non fiable d'atteindre le modele sans traverser ce filtre.

**Le moment de l'agregation.** Elle intervient **apres** les appels, sur les
resultats — pas avant, sur le code. C'est l'inverse de ce que montre le schema de
flux existant, qui place l'agregation en amont du prompt.

**La limite du reessai.** Le prompt reconstruit est identique a chaque tentative.
Renvoyer la meme requete a un modele qui vient de mal repondre produit souvent la
meme reponse. Ajouter l'erreur precedente au prompt (« ta reponse n'etait pas un
JSON valide, respecte strictement ce schema ») corrige dans la majorite des cas,
et represente une trentaine de lignes.
