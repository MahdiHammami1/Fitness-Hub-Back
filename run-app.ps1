#!/usr/bin/env pwsh
# Script de lancement - Wouhouch Hub Backend
# Ce script simplifie le lancement de l'application avec les profils corrects

param(
    [string]$profile = "dev",
    [switch]$clean = $false,
    [switch]$test = $false
)

Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Wouhouch Hub Backend Launcher" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier que Maven est installé
$mavenPath = mvn --version 2>$null
if ($null -eq $mavenPath) {
    Write-Host "❌ Maven n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    Write-Host "   Installez Maven depuis: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Maven trouvé:" -ForegroundColor Green
Write-Host "   $($mavenPath.Split([Environment]::NewLine)[0])" -ForegroundColor Gray
Write-Host ""

# Mode Test
if ($test) {
    Write-Host "🧪 Mode TEST" -ForegroundColor Yellow
    Write-Host "   Exécution des tests unitaires..." -ForegroundColor Gray
    Write-Host ""
    mvn test
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Tests réussis !" -ForegroundColor Green
    } else {
        Write-Host "❌ Erreur lors de l'exécution des tests" -ForegroundColor Red
    }
    exit $LASTEXITCODE
}

# Mode Clean
if ($clean) {
    Write-Host "🧹 Nettoyage du projet..." -ForegroundColor Yellow
    mvn clean
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Erreur lors du nettoyage" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Nettoyage terminé" -ForegroundColor Green
    Write-Host ""
}

# Validation du profil
$validProfiles = @("dev", "prod")
if ($profile -notin $validProfiles) {
    Write-Host "❌ Profil invalide: $profile" -ForegroundColor Red
    Write-Host "   Profils valides: $($validProfiles -join ', ')" -ForegroundColor Yellow
    exit 1
}

# Déterminer l'environnement
switch ($profile) {
    "dev" {
        $profileName = "DÉVELOPPEMENT"
        $color = "Yellow"
    }
    "prod" {
        $profileName = "PRODUCTION"
        $color = "Red"
    }
}

Write-Host "🚀 Lancement en mode: $profileName" -ForegroundColor $color
Write-Host "   Profil actif: $profile" -ForegroundColor Gray
Write-Host ""

Write-Host "⏳ Démarrage de l'application..." -ForegroundColor Cyan
Write-Host "   Attendez le message: 'Started DemoApplication'" -ForegroundColor Gray
Write-Host "   (Cela peut prendre 10-20 secondes)" -ForegroundColor Gray
Write-Host ""

# Lancer l'application
$arguments = @(
    "spring-boot:run",
    "-Dspring-boot.run.arguments=--spring.profiles.active=$profile"
)

mvn $arguments

$exitCode = $LASTEXITCODE

Write-Host ""
if ($exitCode -eq 0) {
    Write-Host "✅ Application fermée normalement" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur lors du démarrage (Code: $exitCode)" -ForegroundColor Red
}

exit $exitCode

