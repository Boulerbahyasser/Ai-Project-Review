# `report.render` — Rendu vers un format

Pattern **Strategy** : un rendu par format de sortie. Le LaTeX est obligatoire
(section 7), le HTML est une extension optionnelle (section 18).

## Contenu

| Classe | Role |
|---|---|
| `ReportRenderer` | Contrat : `format()` + `render(rapport) -> String` |
| `LatexReportRenderer` | Rendu LaTeX — **le format exige** |
| `HtmlReportRenderer` | Rendu HTML — optionnel |
| `ReportRendererRegistry` | Selectionne le rendu selon `config/application.yaml` |

## Regle absolue du rendu LaTeX

**Tout texte** venant du projet analyse ou du modele passe par
`report.latex.LatexEscaper` avant insertion. Un nom de classe contenant `_`, un
commentaire contenant `%` ou `$`, et la compilation echoue — ou pire, produit un
document au contenu altere.

C'est aussi une question de securite : le texte du projet analyse est non fiable
(section 8). Du LaTeX injecte dans un rapport peut lire des fichiers de la machine
si la compilation autorise `\input` ou `\write18`.

## Ajouter un format (section 10)

Une implementation de `ReportRenderer` + son enregistrement dans le registre.
Aucun fichier existant modifie. C'est un des six points d'extension mesures.

## Ne va pas ici

- La decision de **ce qui** figure au rapport : c'est `report.assembly`.
- La compilation en PDF : c'est `report.compile`.
