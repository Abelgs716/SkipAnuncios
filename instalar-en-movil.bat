@echo off
rem Instala AutoSkip en el movil conectado por USB y activa su servicio de Accesibilidad.
setlocal
set ADB=%USERPROFILE%\AndroidDev\sdk\platform-tools\adb.exe
cd /d "%~dp0"

"%ADB%" start-server >nul
"%ADB%" wait-for-device
echo Movil detectado. Instalando AutoSkip...
"%ADB%" install -r AutoSkip-1.0.apk || goto :error

echo Activando el servicio de Accesibilidad...
"%ADB%" push scripts\enable-service.sh /data/local/tmp/autoskip-enable.sh >nul || goto :error
"%ADB%" shell sh /data/local/tmp/autoskip-enable.sh || goto :error
"%ADB%" shell rm /data/local/tmp/autoskip-enable.sh

"%ADB%" shell am start -n com.autoskip.app/.MainActivity >nul
echo.
echo Listo. AutoSkip esta instalado y activo en el movil.
pause
exit /b 0

:error
echo.
echo Algo ha fallado. Revisa que la depuracion USB este activada y que hayas aceptado el aviso en el movil.
pause
exit /b 1
