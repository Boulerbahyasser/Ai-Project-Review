# `report.latex` — Gabarit et echappement LaTeX

## Contenu

| Classe | Role |
|---|---|
| `LatexEscaper` | Contrat d'echappement du texte non fiable |
| `DefaultLatexEscaper` | Echappement des caracteres speciaux |
| `LatexTemplate` | Gabarit charge des ressources, substitution `{{cle}}` |

## Les caracteres a echapper

| Caractere | Remplacement |
|---|---|
| `&` `%` `$` `#` `_` `{` `}` | prefixer par une barre oblique inverse |
| `~` | `\textasciitilde{}` |
| `^` | `\textasciicircum{}` |
| barre oblique inverse | `\textbackslash{}` |

L'ordre compte : la barre oblique inverse doit etre traitee **en premier**, sinon
on echappe les barres qu'on vient d'ajouter.

## Pourquoi c'est une question de securite, pas de confort

Le texte insere provient du projet analyse — donc **non fiable** (section 8).
Sans echappement, un commentaire du type `\input{/etc/passwd}` dans le code
analyse serait interprete a la compilation. La section 7 le dit :
*« la compilation automatique du .tex en PDF ne doit pas compromettre la securite
de l'application »*.

Ne jamais compiler avec `--shell-escape`.

## Le gabarit

`src/main/resources/templates/report.tex.template` contient le squelette du
document. Le sortir du code Java permet de le modifier sans recompiler, et de
suivre son evolution dans Git. C'est le squelette **fixe** que la section 7 exige :
il ne depend pas du modele.

## Test le plus utile a ecrire ici

Donner a l'echappeur une chaine contenant les dix caracteres speciaux et verifier
la sortie. C'est un test pur, sans dependance, qui protege un point de securite.
