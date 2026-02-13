@echo off
REM Script de lancement - Wouhouch Hub Backend
REM Utilisation: run-app.bat [dev|prod] [--clean] [--test]

setlocal enabledelayedexpansion

set PROFILE=dev
set CLEAN=0
set TEST=0

REM Parser les arguments
for %%A in (%*) do (
    if "%%A"=="dev" set PROFILE=dev
    if "%%A"=="prod" set PROFILE=prod
    if "%%A"=="--clean" set CLEAN=1
    if "%%A"=="--test" set TEST=1
)

cls
echo ================================
echo   Wouhouch Hub Backend Launcher
echo ================================
echo.

REM Vérifier Maven
maven --version >nul 2>&1
if errorlevel 1 (
    echo. [ERROR] Maven n'est pas installé ou pas dans le PATH
    echo. Installez Maven depuis: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo. [OK] Maven trouvé
echo.

REM Mode Test
if %TEST% equ 1 (
    echo. [INFO] Mode TEST
    echo. Exécution des tests unitaires...
    echo.
    call mvn test
    if errorlevel 1 (
        echo. [ERROR] Erreur lors de l'exécution des tests
        exit /b 1
    )
    echo. [OK] Tests réussis !
    pause
    exit /b 0
)

REM Mode Clean
if %CLEAN% equ 1 (
    echo. [INFO] Nettoyage du projet...
    call mvn clean
    if errorlevel 1 (
        echo. [ERROR] Erreur lors du nettoyage
        exit /b 1
    )
    echo. [OK] Nettoyage terminé
    echo.
)

REM Déterminer l'environnement
if "%PROFILE%"=="prod" (
    set PROFILE_NAME=PRODUCTION
) else (
    set PROFILE_NAME=DÉVELOPPEMENT
    set PROFILE=dev
)

echo. [INFO] Lancement en mode: %PROFILE_NAME%
echo. Profil actif: %PROFILE%
echo.

echo. [INFO] Démarrage de l'application...
echo. Attendez le message: 'Started DemoApplication'
echo. (Cela peut prendre 10-20 secondes)
echo.

REM Lancer l'application
call mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=%PROFILE%"

if errorlevel 1 (
    echo.
    echo. [ERROR] Erreur lors du démarrage
    pause
    exit /b 1
) else (
    echo.
    echo. [OK] Application fermée normalement
    pause
)

