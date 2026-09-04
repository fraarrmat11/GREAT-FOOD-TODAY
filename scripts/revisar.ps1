param(
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$RepoRoot = Split-Path -Parent $PSScriptRoot
$BackendDir = Join-Path $RepoRoot 'backend'
$FrontendDir = Join-Path $RepoRoot 'frontend'

function Test-JavaHome21 {
    param([string]$JavaHome)

    if ([string]::IsNullOrWhiteSpace($JavaHome)) {
        return $false
    }

    $ReleaseFile = Join-Path $JavaHome 'release'
    if (-not (Test-Path $ReleaseFile)) {
        return $false
    }

    return (Select-String -Path $ReleaseFile -Pattern 'JAVA_VERSION="21\.' -Quiet)
}

function Use-Java21 {
    $Candidates = @()

    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        $Candidates += $env:JAVA_HOME
    }

    $Candidates += @(
        (Join-Path $env:LOCALAPPDATA 'Programs\Microsoft\jdk-21.0.12.8-hotspot'),
        'C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot',
        'C:\Program Files\Microsoft\jdk-21.0.11.9-hotspot'
    )

    $JavaHome = $Candidates | Where-Object { Test-JavaHome21 $_ } | Select-Object -First 1
    if (-not $JavaHome) {
        throw 'No se encontro un JDK 21. Configura JAVA_HOME apuntando a JDK 21 antes de ejecutar scripts\revisar.ps1.'
    }

    $env:JAVA_HOME = $JavaHome
    $env:Path = (Join-Path $JavaHome 'bin') + [IO.Path]::PathSeparator + $env:Path
    Write-Host "Usando Java: $JavaHome"
}

function Invoke-Check {
    param(
        [string]$Name,
        [scriptblock]$Command
    )

    Write-Host ""
    Write-Host "==> $Name"
    & $Command

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo: $Name"
    }
}

Use-Java21

Invoke-Check 'Backend: tests Maven' {
    Push-Location $BackendDir
    try {
        & .\mvnw.cmd test
    }
    finally {
        Pop-Location
    }
}

if (-not $SkipInstall -and -not (Test-Path (Join-Path $FrontendDir 'node_modules'))) {
    Invoke-Check 'Frontend: instalar dependencias' {
        Push-Location $FrontendDir
        try {
            & npm ci
        }
        finally {
            Pop-Location
        }
    }
}

Invoke-Check 'Frontend: build Angular' {
    Push-Location $FrontendDir
    try {
        & npm run build
    }
    finally {
        Pop-Location
    }
}

Invoke-Check 'Frontend: tests Angular CI' {
    Push-Location $FrontendDir
    try {
        & npm run test:ci
    }
    finally {
        Pop-Location
    }
}

Write-Host ""
Write-Host 'Revision completada correctamente.'