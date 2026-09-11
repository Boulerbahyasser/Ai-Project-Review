# `project.model` — Representation du projet analyse

Pattern **Composite** : un repertoire et un fichier sont manipules uniformement.
La recursion vit dans la structure de donnees, pas dans les algorithmes.

## Contenu

| Classe | Role |
|---|---|
| `ProjectNode` | Interface scellee : `permits FileNode, DirectoryNode` |
| `FileNode` | Feuille : nom, chemin, taille, type |
| `DirectoryNode` | Noeud : une seule liste `List<ProjectNode>`, ordre du disque preserve |
| `FileType` | Java, test, config, Maven, Gradle, Docker, doc, script, ressource, autre |
| `ProjectOrigin` | Repertoire, archive, Git |
| `ProjectMetadata` | Nom, emplacement, origine, date d'import |
| `SoftwareProject` | Metadonnees + racine + `allFiles()` |

## Pourquoi une seule liste d'enfants

Separer `List<FileNode>` et `List<DirectoryNode>` ferait perdre l'ordre du disque :
l'IHM afficherait tous les repertoires groupes a part, et l'arbre ne ressemblerait
plus au projet reel.

## Pourquoi `sealed`

Le compilateur connait la liste complete des types. Un `switch` sur un
`ProjectNode` n'a donc pas besoin de `default`, et si un troisieme type de noeud
etait ajoute, **tout code qui ne le traite pas refuserait de compiler**. L'oubli
d'un cas devient impossible.

## Regle sur le contrat commun

`ProjectNode` ne declare que ce qui a un sens **pour les deux** : `name()`,
`path()`, `sizeInBytes()`. Ne jamais y ajouter `children()` (un fichier n'en a
pas) ni `type()` (un repertoire n'en a pas) : ces methodes restent sur leur type,
et le filtrage de motif y accede quand c'est necessaire.

## Avertissement

Le contenu decrit ici provient d'un projet **non fiable** (section 8). Ces classes
ne portent que des metadonnees ; elles n'executent ni n'interpretent rien.
