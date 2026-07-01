@echo off
chcp 65001 >nul
setlocal

cd /d "%~dp0"

if not exist "baslatici\HonorMC-Baslatici.jar" (
  echo HonorMC baslatici bulunamadi: baslatici\HonorMC-Baslatici.jar
  echo Tam dagitim paketini kullandigindan emin ol.
  pause
  exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
  echo Java bulunamadi. HonorMC icin Java 25 veya daha yeni bir runtime gerekir.
  pause
  exit /b 1
)

java -jar "baslatici\HonorMC-Baslatici.jar" --ayar "ayarlar\baslatici.properties" %*
set "HONORMC_CIKIS=%ERRORLEVEL%"
pause
exit /b %HONORMC_CIKIS%
