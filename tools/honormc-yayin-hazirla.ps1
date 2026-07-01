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

function Get-HonorMCTagChangelog {
    param(
        [Parameter(Mandatory)]
        [string] $CurrentTag,

        [Parameter(Mandatory)]
        [string] $Project
    )

    $previousTag = & git -C $repoRoot tag -l "Honor-*" --sort=version:refname
    if ($previousTag.Count -gt 0) {
        $previousTag = $previousTag[-1]
    } else {
        $previousTag = ""
    }

    if ($previousTag) {
        $gitRange = "${previousTag}..HEAD"
        $comparisonLine = "karsilastirma: https://github.com/$Project/compare/$previousTag...$CurrentTag"
    } else {
        $gitRange = "HEAD"
        $comparisonLine = "karsilastirma: https://github.com/$Project/releases/tag/$CurrentTag"
    }

    $logLines = @(& git -C $repoRoot log --pretty=format:"%h|%s" --no-decorate $gitRange)
    if (-not $logLines) {
        $logLines = @("00000000|Ilk yayin veya log bulunamadi")
    }

    $added = @()
    $removed = @()
    $changed = @()
    foreach ($line in $logLines) {
        if ($line -notmatch "^([0-9a-f]{7,})\|(.*)$") {
            continue
        }

        $hash = $matches[1]
        $msg = $matches[2]
        $lower = $msg.ToLowerInvariant()
        $link = "https://github.com/$Project/commit/$hash"
        $entry = "- [${hash}]($link) ${msg}"

        if ($lower -match "(feat|feature|add(ed)?|yeni|add|ekle|eklendi|yenilik|support|perf|optimizer|refactor)") {
            $added += "+ " + $entry
        }
        elseif ($lower -match "(remove|removed|delete(d)?|kaldir|kaldiril|sil|silme|temizle|drop|dropped|silindi|kaldirildi|retire|cikart|cikar|cikarildi)") {
            $removed += "- " + $entry
        }
        else {
            $changed += "~ " + $entry
        }
    }

    $summary = @(
        "HonorMC surum ozeti",
        "## Indirme Secenekleri ($comparisonLine)",
        "",
        "## + Ne Geldi",
        ""
    )
    if ($added.Count -gt 0) { $summary += $added } else { $summary += "- Bu surumde yeni ozellik kaydi yok." }
    $summary += "", "## - Ne Gitti", ""
    if ($removed.Count -gt 0) { $summary += $removed } else { $summary += "- Bu surumde cikarilan bir ozellik kaydi yok." }
    $summary += "", "## ~ Ne Duzenlendi", ""
    if ($changed.Count -gt 0) { $summary += $changed } else { $summary += "- Bu surumde duzenlenen bir kod kaydi bulunamadi." }
    $summary += "", "## Gecis Karsilastirmasi", "- ${comparisonLine}"

    return $summary
}

if (-not $NoBuild) {
    Write-Host "Yapi alani hazirlaniyor..."
    & (Join-Path $repoRoot "gradlew.bat") applyAllPatches packageHonorMCJar paketleHonorMCDagitim zipHonorMCDagitim --stacktrace
}

Write-Host "Git etiketi olusturuluyor: $Version"
Write-Host "Etiket aciklamasi hazirlaniyor..."
$repoSlug = Get-GitRepository
$tagSummary = Get-HonorMCTagChangelog -CurrentTag $Version -Project $repoSlug
$tagSummaryPath = Join-Path $repoRoot "release-notes-${Version}-tag.txt"
Set-Content -Path $tagSummaryPath -Value $tagSummary -Encoding UTF8
& git -C $repoRoot tag -a $Version -F $tagSummaryPath
Remove-Item -Path $tagSummaryPath -Force

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
