# Construit les trois images Docker utilisees par l'application (section 8).
# Noms alignes sur config/application.yaml : ne pas changer ici sans changer la-bas.

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker n'est pas installe ou n'est pas sur le PATH. Installez Docker Desktop puis relancez ce script."
    exit 1
}

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker est installe mais le demon ne repond pas (Docker Desktop est-il lance ?)."
    exit 1
}

$images = @(
    @{ Name = "ai-reviewer/sandbox:latest"; Context = "docker/sandbox" },
    @{ Name = "ai-reviewer/latex:latest";   Context = "docker/latex" },
    @{ Name = "ai-reviewer/unzip:latest";   Context = "docker/unzip" }
)

foreach ($image in $images) {
    Write-Host "==> docker build -t $($image.Name) $($image.Context)"
    docker build -t $image.Name $image.Context
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Echec de la construction de $($image.Name)"
        exit 1
    }
}

Write-Host "Images construites : $(($images | ForEach-Object { $_.Name }) -join ', ')"
