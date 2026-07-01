param(
    [string] $Version = "26.2"
)

$ErrorActionPreference = "Stop"

$purpurUrl = "https://api.purpurmc.org/v2/purpur/$Version"
$paperUrl = "https://fill.papermc.io/v3/projects/paper/versions/$Version/builds"

$purpur = Invoke-RestMethod -Uri $purpurUrl
$paper = Invoke-RestMethod -Uri $paperUrl

$latestPurpurBuild = $purpur.builds.latest
$paperBuilds = @($paper)
if ($paperBuilds.Count -eq 1 -and ($paperBuilds[0].PSObject.Properties.Name -contains "value")) {
    $paperBuilds = @($paperBuilds[0].value)
}
$latestPaperBuild = ($paperBuilds | Sort-Object -Property @{ Expression = { [int] $_.id } } -Descending | Select-Object -First 1)

$lines = @(
    "# HonorMC upstream kontrolu",
    "",
    "- Hedef surum: $Version",
    "- Purpur en yeni build: $latestPurpurBuild",
    "- Paper en yeni build: $($latestPaperBuild.id)",
    "- Paper kanal: $($latestPaperBuild.channel)",
    "- Paper yayin zamani: $($latestPaperBuild.time)",
    "",
    "HonorMC release karari icin Purpur build numarasi ve Paper kanali birlikte kontrol edilmelidir."
)

$lines | ForEach-Object { Write-Output $_ }

if ($env:GITHUB_STEP_SUMMARY) {
    $lines | Out-File -FilePath $env:GITHUB_STEP_SUMMARY -Encoding utf8 -Append
}
