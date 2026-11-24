[CmdletBinding()]
param(
    [ValidateSet('app-image', 'exe', 'msi')]
    [string]$PackageType = 'app-image',
    [switch]$Console
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendPath = Join-Path $projectRoot 'frontend'
$outputPath = Join-Path $projectRoot 'windows-app'
$jarPath = Join-Path $projectRoot 'target\electro-accountant-backend-1.0.2.jar'

foreach ($command in @('java', 'mvn', 'npm', 'jpackage')) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "'$command' was not found. Install a Windows JDK 21 (not only a JRE), Maven, and Node.js before building the desktop package."
    }
}

Push-Location $frontendPath
try {
    npm install
    npm run build
}
finally {
    Pop-Location
}

Push-Location $projectRoot
try {
    mvn -DskipTests clean package
}
finally {
    Pop-Location
}

if (-not (Test-Path -LiteralPath $jarPath)) {
    throw "Expected application jar was not produced: $jarPath"
}

if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath $env:JAVA_HOME)) {
    throw "JAVA_HOME must point to the JDK that runs jpackage."
}

if (Test-Path -LiteralPath $outputPath) {
    Remove-Item -LiteralPath $outputPath -Recurse -Force
}
New-Item -ItemType Directory -Path $outputPath | Out-Null

$jpackageArguments = @(
    '--type', $PackageType,
    '--name', 'Electro Accountant',
    '--app-version', '1.0.2',
    '--vendor', 'Electro Accountant',
    '--description', 'Local-first accounting application',
    '--input', (Join-Path $projectRoot 'target'),
    '--main-jar', (Split-Path -Leaf $jarPath),
    '--dest', $outputPath,
    # Use the complete JDK runtime. Spring Boot is an executable fat jar, so its
    # nested dependencies cannot be reliably analyzed by jlink during packaging.
    # A larger installer is preferable to a launcher that cannot start its JVM.
    '--runtime-image', $env:JAVA_HOME,
    '--java-options', '-Delectro.accountant.open-browser=true',
    '--java-options', '-Delectro.accountant.desktop-mode=true',
    '--java-options', '-Dserver.address=127.0.0.1'
)

if ($PackageType -ne 'app-image') {
    $jpackageArguments += @('--win-dir-chooser', '--win-menu', '--win-shortcut')
}

if ($Console) {
    $jpackageArguments += '--win-console'
}

& jpackage @jpackageArguments
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

Write-Host "Windows package created in: $outputPath"
if ($PackageType -eq 'app-image') {
    Write-Host "Launch: $outputPath\Electro Accountant\Electro Accountant.exe"
}
