$ErrorActionPreference = "Stop"
Set-Location -LiteralPath $PSScriptRoot

$baslatici = Join-Path $PSScriptRoot "baslatici\HonorMC-Baslatici.jar"
if (!(Test-Path -LiteralPath $baslatici)) {
    Write-Host "HonorMC Baslatici bulunamadi: $baslatici" -ForegroundColor Red
    exit 1
}

& java -jar $baslatici --ayar "ayarlar\baslatici.properties" @args
