# `configuration` - Parametres de l'application

## Contenu attendu

- Objets de configuration immuables (LLM, analyse, bac a sable, rapport, persistance).
- Chargement depuis `config/application.yaml` + validation des valeurs.
- Acces aux secrets via les **variables d'environnement** uniquement.

## Regles absolues

- Aucune cle d'API dans le depot (section 17). Le fichier de configuration ne
  contient que le *nom* de la variable d'environnement a lire.
- Aucune valeur en dur dispersee dans le code : tout parametre passe par ici.
