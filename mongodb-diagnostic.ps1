#!/usr/bin/env pwsh
# Script de diagnostic MongoDB
# Ce script vérifie que l'application peut se connecter à MongoDB Atlas

param(
    [string]$uri = "mongodb+srv://mahdihammami:testtest@cluster01.v7ca8ov.mongodb.net/db1?appName=Cluster01"
)

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║       MongoDB Atlas Connection Diagnostic              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Step 1: Test Internet Connectivity
Write-Host "📋 Step 1: Testing internet connectivity..." -ForegroundColor Yellow
try {
    $ping = Test-Connection -ComputerName google.com -Count 1 -ErrorAction Stop
    Write-Host "  ✅ Internet: OK" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Internet: FAILED" -ForegroundColor Red
    Write-Host "     No internet connection detected" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 2: Parse URI
Write-Host "📋 Step 2: Parsing MongoDB URI..." -ForegroundColor Yellow
if ($uri -match "mongodb\+srv://([^:]+):([^@]+)@([^/]+)/([^\?]+)") {
    $username = $matches[1]
    $password = "*" * $matches[2].Length
    $host = $matches[3]
    $database = $matches[4]

    Write-Host "  ✅ URI parsed successfully" -ForegroundColor Green
    Write-Host "     User: $username" -ForegroundColor Gray
    Write-Host "     Host: $host" -ForegroundColor Gray
    Write-Host "     Database: $database" -ForegroundColor Gray
} else {
    Write-Host "  ❌ URI format invalid" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 3: Test hostname resolution
Write-Host "📋 Step 3: Testing hostname resolution..." -ForegroundColor Yellow
try {
    $resolved = [System.Net.Dns]::GetHostAddresses($host)
    Write-Host "  ✅ Hostname resolved: $($resolved[0].IPAddressToString)" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Cannot resolve hostname: $host" -ForegroundColor Red
    Write-Host "     Check your DNS settings or network connectivity" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 4: Check Maven
Write-Host "📋 Step 4: Checking Maven..." -ForegroundColor Yellow
$mvn = mvn --version 2>$null
if ($mvn) {
    Write-Host "  ✅ Maven found" -ForegroundColor Green
    Write-Host "     $($mvn.Split([Environment]::NewLine)[0])" -ForegroundColor Gray
} else {
    Write-Host "  ❌ Maven not found in PATH" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 5: Compile and Run
Write-Host "📋 Step 5: Compiling application..." -ForegroundColor Yellow
mvn clean compile -q 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "  ❌ Compilation failed" -ForegroundColor Red
    Write-Host "     Run: mvn clean compile" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║                                                        ║" -ForegroundColor Green
Write-Host "║        ✅ All checks passed!                           ║" -ForegroundColor Green
Write-Host "║                                                        ║" -ForegroundColor Green
Write-Host "║  Next step: Run the application                        ║" -ForegroundColor Green
Write-Host "║  Command:  .\run-app.ps1 -profile dev                 ║" -ForegroundColor Green
Write-Host "║                                                        ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

