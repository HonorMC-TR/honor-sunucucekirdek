@echo off
setlocal
cd /d "%~dp0"

if not exist "baslatici\HonorMC-Baslatici.jar" (
  echo HonorMC Baslatici bulunamadi: baslatici\HonorMC-Baslatici.jar
  pause
  exit /b 1
)

java -jar "baslatici\HonorMC-Baslatici.jar" --ayar "ayarlar\baslatici.properties" %*
pause
