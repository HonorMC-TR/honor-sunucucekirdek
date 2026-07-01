$ErrorActionPreference = "Stop"

$kokDizin = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $kokDizin

$baslatici = Join-Path $kokDizin "baslatici/HonorMC-Baslatici.jar"
if (-not (Test-Path $baslatici)) {
    Write-Host "HonorMC baslatici bulunamadi: baslatici/HonorMC-Baslatici.jar" -ForegroundColor Red
    Write-Host "Tam dagitim paketini kullandigindan emin ol." -ForegroundColor Yellow
    exit 1
}

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    Write-Host "Java bulunamadi. HonorMC icin Java 25 veya daha yeni bir runtime gerekir." -ForegroundColor Red
    exit 1
}

& java -jar $baslatici --ayar "ayarlar/baslatici.properties" @args
exit $LASTEXITCODE
