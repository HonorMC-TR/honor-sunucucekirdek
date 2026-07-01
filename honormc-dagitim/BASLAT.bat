@echo off
chcp 65001 >nul
setlocal

cd /d "%~dp0"

set "HONOR_JAR="
for /f "delims=" %%F in ('dir /b /a-d /o-n "cekirdek\Honor-*.jar" 2^>nul') do (
  set "HONOR_JAR=cekirdek\%%F"
  goto :HONOR_JAR_BULUNDU
)

:HONOR_JAR_BULUNDU
if "%HONOR_JAR%"=="" (
  echo Honor jar bulunamadi: cekirdek\Honor-*.jar
  pause
  exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
  echo Java bulunamadi. HonorMC icin Java 25 veya daha yeni bir runtime gerekir.
  pause
  exit /b 1
)

if "%HONORMC_MIN_RAM%"=="" set "HONORMC_MIN_RAM=8G"
if "%HONORMC_MAX_RAM%"=="" set "HONORMC_MAX_RAM=16G"

echo HonorMC baslatiliyor...
echo RAM: %HONORMC_MIN_RAM% - %HONORMC_MAX_RAM%

java ^
  --enable-native-access=ALL-UNNAMED ^
  --illegal-native-access=allow ^
  --sun-misc-unsafe-memory-access=allow ^
  -Xms%HONORMC_MIN_RAM% ^
  -Xmx%HONORMC_MAX_RAM% ^
  -Dfile.encoding=UTF-8 ^
  -Duser.language=tr ^
  -Duser.country=TR ^
  -jar "%HONOR_JAR%" ^
  --nogui ^
  --config "ayarlar\sunucu.properties" ^
  --plugins "eklentiler" ^
  --world-dir "dunyalar" ^
  --world "ana-dunya" ^
  --bukkit-settings "ayarlar\bukkit-uyumluluk.yml" ^
  --spigot-settings "ayarlar\spigot-uyumluluk.yml" ^
  --paper-settings-directory "ayarlar\paper" ^
  --paper-settings "ayarlar\paper-eski-uyumluluk.yml" ^
  --purpur-settings "ayarlar\purpur-uyumluluk.yml" ^
  --commands-settings "komutlar.yml"

set "HONORMC_CIKIS=%ERRORLEVEL%"
pause
exit /b %HONORMC_CIKIS%
