param(
    [string] $Version = "",
    [string] $Remote = "origin",
    [switch] $NoPush,
    [switch] $NoBuild
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Get-HonorMCDefaultVersion {
    $repoRoot = Split-Path -Parent $PSScriptRoot
    $gradlePropertiesPath = Join-Path $repoRoot "gradle.properties"
    if (-not (Test-Path $gradlePropertiesPath)) {
        throw "gradle.properties bulunamadi: $gradlePropertiesPath"
    }

    $mcLine = Get-Content -Path $gradlePropertiesPath | Where-Object { $_ -match "^mcVersion\\s*=" } | Select-Object -First 1
    if ($mcLine -notmatch "^mcVersion\\s*=\\s*(.+)$") {
        throw "gradle.properties icinde mcVersion bulunamadi."
    }

    return "Honor-$($matches[1].Trim())"
}

function Get-GitRepository {
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

if (-not $Version) {
    $Version = Get-HonorMCDefaultVersion
}

if ($Version -notlike "Honor-*") {
    throw "Version etiket format hatasi. Ornek: Honor-26.2"
}

$repo = Get-GitRepository
$repoRoot = Split-Path -Parent $PSScriptRoot

if (& git -C $repoRoot tag -l $Version) {
    throw "Bu etiket zaten var: $Version"
}

if (-not $NoBuild) {
    Write-Host "Yapi alani hazirlaniyor..."
    & (Join-Path $repoRoot "gradlew.bat") applyAllPatches packageHonorMCJar paketleHonorMCDagitim zipHonorMCDagitim --stacktrace
}

Write-Host "Git etiketi olusturuluyor: $Version"
& git -C $repoRoot tag $Version

if (-not $NoPush) {
    Write-Host "Tag push ediliyor: $Remote $Version"
    & git -C $repoRoot push $Remote $Version
    Write-Host "Release otomatik akis tetiklendi."
} else {
    Write-Host "NoPush secili. Onceden etiketlenmis tag'in pushini elle yapin."
    Write-Host "Ornek: git -C $repoRoot push $Remote $Version"
}

Write-Host "Yayin bilgisi:"
Write-Host "  Repo        : https://github.com/$repo"
Write-Host "  Etiket      : $Version"
Write-Host "  Release URL : https://github.com/$repo/releases"
Write-Host ""
Write-Host "Release notlarinda otomatik su format kullanilacak:"
Write-Host " - Indirme Secenekleri"
Write-Host " - + Ne Geldi"
Write-Host " - - Ne Gitti"
Write-Host " - ~ Ne Duzenlendi"
Write-Host ""
Write-Host "Arsiv secenekleri:"
Write-Host " - Honor-<versiyon>.jar"
Write-Host " - Honor-<versiyon>-baslat.bat"
Write-Host " - Honor-<versiyon>-dagitim.zip"
Write-Host "Ornek: gh workflow run release.yml --ref main -f version=$Version"
