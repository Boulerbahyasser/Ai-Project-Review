#!/usr/bin/env bash
# Construit les trois images Docker utilisees par l'application (section 8).
# Noms alignes sur config/application.yaml : ne pas changer ici sans changer la-bas.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if ! command -v docker >/dev/null 2>&1; then
    echo "Erreur : Docker n'est pas installe ou n'est pas sur le PATH." >&2
    echo "Installez Docker Desktop puis relancez ce script." >&2
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "Erreur : Docker est installe mais le demon ne repond pas (Docker Desktop est-il lance ?)." >&2
    exit 1
fi

echo "==> docker build -t ai-reviewer/sandbox:latest docker/sandbox"
docker build -t ai-reviewer/sandbox:latest docker/sandbox

echo "==> docker build -t ai-reviewer/latex:latest docker/latex"
docker build -t ai-reviewer/latex:latest docker/latex

echo "==> docker build -t ai-reviewer/unzip:latest docker/unzip"
docker build -t ai-reviewer/unzip:latest docker/unzip

echo "Images construites : ai-reviewer/sandbox:latest, ai-reviewer/latex:latest, ai-reviewer/unzip:latest"
