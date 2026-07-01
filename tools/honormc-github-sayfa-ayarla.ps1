param(
    [string] $Repo = "",
    [string] $Description = "Türkiye odakli, tamamen Turkce ve yuksek performansli Minecraft cekirdek ekosistemi: HonorMC.",
    [string] $Homepage = "",
    [string[]] $Topics = @("honormc", "minecraft", "minecraft-server", "purpur-fork", "turkish")
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Get-RepositorySlug {
    param([string] $InputRepo)

    if ($InputRepo) {
        return $InputRepo
    }

    $repoRoot = Split-Path -Parent $PSScriptRoot
    $remoteUrl = (& git -C $repoRoot remote get-url origin).Trim()
    if (-not $remoteUrl) {
        throw "git remote 'origin' bulunamadi."
    }

    $normalized = $remoteUrl -replace "\\.git$", ""
    if ($normalized -match "github\\.com[:/](.+/.+)") {
        return $matches[1]
    }

    throw "origin URL parse edilemedi: $remoteUrl"
}

$repoSlug = Get-RepositorySlug -InputRepo $Repo
if (-not $Homepage) {
    $owner = ($repoSlug -split "/")[0]
    $project = ($repoSlug -split "/")[1]
    $pageName = $project.ToLower()
    $ownerName = $owner.ToLower()
    $Homepage = "https://$ownerName.github.io/$pageName/"
}

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Host "GitHub CLI (gh) bulunamadi."
    Write-Host "Ayni ayarlari elle yapmak icin:"
    Write-Host "gh repo edit $repoSlug --description `"$Description`" --homepage `"$Homepage`""
    foreach ($topic in $Topics) {
        Write-Host "gh repo edit $repoSlug --add-topic $topic"
    }
    return
}

Write-Host "Repo ayarlari guncelleniyor: $repoSlug"
Write-Host "Aciklama: $Description"
Write-Host "Proje sayfasi: $Homepage"
& gh repo edit $repoSlug --description $Description --homepage $Homepage

foreach ($topic in $Topics) {
    & gh repo edit $repoSlug --add-topic $topic | Out-Null
}

Write-Host "Repo basariyla guncellendi."
Write-Host "Repo: https://github.com/$repoSlug"
