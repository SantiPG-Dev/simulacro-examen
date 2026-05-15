@echo off
REM ====================================================
REM  Build installer para Simulacro de Examen
REM  Genera instalador .exe con JavaFX incluido
REM ====================================================
title Construyendo Simulacro de Examen...

set VERSION=1.0.0
set APP_NAME=Simulacro de Examen
set VENDOR=SantiPG-Dev
set OUTPUT_DIR=installer

REM --- Buscar Liberica JDK (con JavaFX) ---
set JPACKAGE=
if exist "%USERPROFILE%\java\jdk-21.0.5-full\bin\jpackage.exe" (
    set JPACKAGE="%USERPROFILE%\java\jdk-21.0.5-full\bin\jpackage.exe"
)
if "%JPACKAGE%"=="" (
    where jpackage >nul 2>nul
    if %ERRORLEVEL% EQU 0 set JPACKAGE=jpackage
)

if "%JPACKAGE%"=="" (
    echo ERROR: No se encuentra jpackage.
    echo Descarga Liberica JDK Full desde: https://bell-sw.com/pages/downloads/
    pause
    exit /b 1
)

echo ========================================
echo  Simulacro de Examen - Build Installer
echo ========================================
echo.

echo [1/4] Compilando aplicacion...
cd /d "%~dp0"
call mvn clean package -q -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo la compilacion.
    pause
    exit /b 1
)
echo  OK - JAR generado.

echo [2/4] Generando icono...
if not exist app.ico (
    where python >nul 2>nul
    if %ERRORLEVEL% EQU 0 (
        python gen_icon.py
        if exist app.ico (
            echo  OK - Icono generado.
        ) else (
            echo  Sin icono personalizado.
        )
    ) else (
        echo  Sin icono (Python no disponible).
    )
) else (
    echo  OK - Usando icono existente.
)

echo [3/4] Buscando WiX...
set WIX_BIN=%USERPROFILE%\wix-tools
set WIX_FOUND=
if exist "%WIX_BIN%\light.exe" set WIX_FOUND=1
where light.exe >nul 2>nul && set WIX_FOUND=1

if not defined WIX_FOUND (
    echo  WiX no encontrado. Descargando binarios...
    mkdir "%WIX_BIN%" 2>nul
    curl -sL -o "%TEMP%\wix.zip" "https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip"
    powershell -Command "Expand-Archive -Path '%TEMP%\wix.zip' -DestinationPath '%WIX_BIN%' -Force" >nul
    if exist "%WIX_BIN%\light.exe" set WIX_FOUND=1
)

if defined WIX_FOUND (
    if not exist "%WIX_BIN%\light.exe" (set WIX_BIN=)
    if defined WIX_BIN set PATH=%PATH%;%WIX_BIN%
    echo  OK - WiX listo.
) else (
    echo  ERROR: No se pudo obtener WiX.
    pause
    exit /b 1
)

echo [4/4] Creando instalador EXE...
if exist %OUTPUT_DIR% rmdir /s /q %OUTPUT_DIR%
mkdir %OUTPUT_DIR%

set ICON_ARG=
if exist app.ico set ICON_ARG=--icon app.ico

%JPACKAGE% --type exe ^
    --input target/ ^
    --main-jar simulacro-examen-%VERSION%.jar ^
    --main-class com.examenes.MainApp ^
    --name "%APP_NAME%" ^
    --app-version %VERSION% ^
    --vendor "%VENDOR%" ^
    --description "Simulacros de examen tipo test para CesurFP" ^
    --win-menu ^
    --win-shortcut ^
    --win-shortcut-prompt ^
    --win-dir-chooser ^
    --win-per-user-install ^
    --add-modules java.desktop,javafx.controls,javafx.fxml ^
    --dest %OUTPUT_DIR%/ ^
    %ICON_ARG%

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Fallo al generar el instalador.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  INSTALADOR GENERADO CON EXITO
echo ========================================
echo.
dir /b "%OUTPUT_DIR%\*.exe" 2>nul
echo.
echo Para instalarlo, haz doble clic en el .exe
echo y sigue los pasos del asistente.
echo.
pause
