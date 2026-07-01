param(
    [string] $RepoUrl = "git@github.com:HonorMC-TR/honor-sunucucekirdek.git",
    [string] $RemoteName = "origin",
    [string] $Branch = "main",
    [switch] $NoCommit
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = Split-Path -Parent $PSScriptRoot

function Ensure-Remote {
    param([string]$Name, [string]$Url)
    if (-not (git -C $repoRoot remote | Select-String -Pattern "^$Name$")) {
        Write-Host "remote '$Name' ekleniyor: $Url"
        git -C $repoRoot remote add $Name $Url
    }
}

function Ensure-Branch {
    param([string]$TargetBranch)
    $current = & git -C $repoRoot branch --show-current
    if ($current -ne $TargetBranch) {
        Write-Host "Gecerli dal: $current"
        if (git -C $repoRoot show-ref --verify --quiet "refs/heads/$TargetBranch") {
            Write-Host "Mevcut $TargetBranch dalina geciliyor."
            git -C $repoRoot checkout $TargetBranch
        } else {
            Write-Host "main bulunamadi, bu dal ile ileriye gonderilecek: $TargetBranch"
            git -C $repoRoot checkout -b $TargetBranch
        }
    }
}

function Ensure-Commit {
    param([string]$Message)
    $status = (git -C $repoRoot status --short)
    if (-not $status) {
        Write-Host "Depoda commit edilmemis degisiklik yok."
        return
    }
    if ($NoCommit) {
        throw "Depoda bekleyen degisiklik var. NoCommit kullaniliyorsa once commit alin."
    }
    Write-Host "Degisiklikler stage'e alinip commit ediliyor: $Message"
    git -C $repoRoot add -A
    git -C $repoRoot commit -m $Message
}

if (-not (Test-Path (Join-Path $repoRoot ".git"))) {
    throw ".git klasoru bulunamadi. Bu dizin bir git deposu degil."
}

Ensure-Remote -Name $RemoteName -Url $RepoUrl
Ensure-Branch -TargetBranch $Branch
Ensure-Commit -Message "HonorMC: publish"
Ensure-Remote -Name $RemoteName -Url $RepoUrl

Write-Host "Github'a push baslatiliyor: $RemoteName $Branch"
git -C $repoRoot push -u $RemoteName $Branch

Write-Host "GitHub Proje Sayfasi ayarlari icin:"
Write-Host "powershell -ExecutionPolicy Bypass -File ./tools/honormc-github-sayfa-ayarla.ps1"
