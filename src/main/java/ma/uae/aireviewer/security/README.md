# `security` - Isolation et code non fiable

Reference : cahier des charges section 8. Le projet analyse est considere comme
**potentiellement malveillant**.

## Contenu attendu

- `sandbox/` : execution isolee dans un conteneur Docker dedie
  (interface + implementation Docker + implementation "execution desactivee" par defaut),
  specification d'execution et limites appliquees.
- `policy/` : verification du principe de moindre privilege avant tout lancement
  (utilisateur non root, systeme de fichiers restreint et en lecture seule quand
  possible, pas de reseau, limites CPU / memoire / processus, duree maximale,
  suppression du conteneur apres analyse).
- `prompt/` : defense contre l'injection de prompt (section 8.2). Le contenu du
  projet est encadre et neutralise : c'est une **donnee**, jamais une instruction.
- `secret/` : masquage des secrets dans les journaux et la trace (section 12).

## Regles absolues

- Jamais d'execution de code du projet analyse sur la machine hote.
- Architecture attendue : Machine virtuelle -> Docker -> Projet evalue.
- L'execution est desactivee par defaut dans `config/application.yaml`.
- Les limites d'execution ne sont pas facultatives : elles sont verifiees avant lancement.
