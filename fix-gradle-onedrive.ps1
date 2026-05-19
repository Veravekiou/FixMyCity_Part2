$ErrorActionPreference = "Stop"

$androidStudioJdk = "C:\Program Files\Android\Android Studio\jbr"
if (Test-Path -LiteralPath $androidStudioJdk) {
    $env:JAVA_HOME = $androidStudioJdk
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

.\gradlew.bat --stop

$workspace = (Resolve-Path ".").Path
$buildPath = Join-Path $workspace "app\build"
$externalBuildPath = Join-Path $env:USERPROFILE ".fixmycity-gradle-build"
$externalAppBuildPath = Join-Path $externalBuildPath "app"

if (Test-Path -LiteralPath $buildPath) {
    $buildItem = Get-Item -LiteralPath $buildPath -Force
    if ($buildItem.LinkType -eq "Junction") {
        [System.IO.Directory]::Delete($buildPath)
    } else {
        attrib -R "$buildPath\*" /S /D
        Remove-Item -LiteralPath $buildPath -Recurse -Force
    }
    Write-Host "Removed $buildPath"
}

if (Test-Path -LiteralPath $externalBuildPath) {
    attrib -R "$externalBuildPath\*" /S /D
    Remove-Item -LiteralPath $externalBuildPath -Recurse -Force
    Write-Host "Removed $externalBuildPath"
}

New-Item -ItemType Directory -Path $externalAppBuildPath -Force | Out-Null
New-Item -ItemType Junction -Path $buildPath -Target $externalAppBuildPath | Out-Null
Write-Host "Linked $buildPath to $externalAppBuildPath"

.\gradlew.bat assembleDebug
