$ErrorActionPreference = "Stop"

$kokDizin = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $kokDizin

foreach ($dizin in @("cekirdek", "eklentiler", "dunyalar", "ayarlar", "ayarlar/paper", "kayitlar", "yedekler")) {
    if (-not (Test-Path $dizin)) {
        New-Item -ItemType Directory -Path $dizin | Out-Null
    }
}

$honorJar = Get-ChildItem -Path "cekirdek" -Filter "Honor-*.jar" -File -ErrorAction SilentlyContinue |
    Sort-Object Name -Descending |
    Select-Object -First 1

if (-not $honorJar) {
    Write-Host "Honor jar bulunamadi: cekirdek/Honor-*.jar" -ForegroundColor Red
    exit 1
}

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    Write-Host "Java bulunamadi. HonorMC icin Java 25 veya daha yeni bir runtime gerekir." -ForegroundColor Red
    exit 1
}

$minRam = if ($env:HONORMC_MIN_RAM) { $env:HONORMC_MIN_RAM } else { "8G" }
$maxRam = if ($env:HONORMC_MAX_RAM) { $env:HONORMC_MAX_RAM } else { "16G" }

$javaArgs = @(
    "--enable-native-access=ALL-UNNAMED",
    "--illegal-native-access=allow",
    "--sun-misc-unsafe-memory-access=allow",
    "-Xms$minRam",
    "-Xmx$maxRam",
    "-Dfile.encoding=UTF-8",
    "-Duser.language=tr",
    "-Duser.country=TR",
    "-jar", $honorJar.FullName,
    "--nogui",
    "--config", "ayarlar/sunucu.properties",
    "--plugins", "eklentiler",
    "--world-dir", "dunyalar",
    "--world", "ana-dunya",
    "--bukkit-settings", "ayarlar/bukkit-uyumluluk.yml",
    "--spigot-settings", "ayarlar/spigot-uyumluluk.yml",
    "--paper-settings-directory", "ayarlar/paper",
    "--paper-settings", "ayarlar/paper-eski-uyumluluk.yml",
    "--purpur-settings", "ayarlar/purpur-uyumluluk.yml",
    "--commands-settings", "ayarlar/komutlar.yml"
)

Write-Host "HonorMC baslatiliyor..."
Write-Host "RAM: $minRam - $maxRam"

& java @javaArgs
exit $LASTEXITCODE
