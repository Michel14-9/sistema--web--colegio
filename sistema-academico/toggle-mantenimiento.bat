@echo off
REM Script para activar/desactivar modo mantenimiento
REM Uso: toggle-mantenimiento.bat [on|off]

set CONFIG_FILE=src\main\resources\config.properties

echo ========================================
echo    MODO MANTENIMIENTO - SISTEMA WEB
echo ========================================

if "%1"=="on" (
    echo sistema.modo-mantenimiento=true > %CONFIG_FILE%
    echo  Modo MANTENIMIENTO activado
    echo    El sistema mostrara la pagina de mantenimiento a todos los usuarios.
) else if "%1"=="off" (
    echo sistema.modo-mantenimiento=false > %CONFIG_FILE%
    echo  Modo MANTENIMIENTO desactivado
    echo    El sistema ya esta operativo para todos los usuarios.
) else (
    echo  Uso: toggle-mantenimiento.bat [on^|off]
    echo    Ejemplo: toggle-mantenimiento.bat on
    echo    Ejemplo: toggle-mantenimiento.bat off
)

echo ========================================
pause
