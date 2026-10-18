#Requires -Version 5.1
<#
.SYNOPSIS
  Clears Allure history and generates a fresh report (optionally after tests).

.PARAMETER Tests
  JUnit --tests filter. Example: io.student.rangiffler.test.profile.ProfileTest

.PARAMETER Tag
  JUnit tag(s), comma-separated -> -DincludeTags. Example: web

.PARAMETER AllTests
  Run all rfr-e2e tests after clean.

.PARAMETER SkipTests
  Only clear history and exit.

.PARAMETER FromExisting
  Only build/open report from current build/allure-results.

.PARAMETER NoServe
  allureReport only (no allureServe).

.EXAMPLE
  .\allure-report.ps1 -Tag web

.EXAMPLE
  .\allure-report.ps1 -Tests io.student.rangiffler.test.profile.ProfileTest

.EXAMPLE
  .\allure-report.ps1 -AllTests
#>
param(
  [string] $Tests = "",
  [string] $Tag = "",
  [switch] $AllTests,
  [switch] $SkipTests,
  [switch] $FromExisting,
  [switch] $NoServe
)

$ErrorActionPreference = "Stop"

$moduleDir = $PSScriptRoot
$repoRoot = Split-Path -Parent $moduleDir
$gradlew = Join-Path $repoRoot "gradlew.bat"
# Clean only results + HTML report. Do NOT delete build\allure (Allure CLI / node.exe).
$resultsDir = Join-Path $moduleDir "build\allure-results"
$reportDirs = @(
  (Join-Path $moduleDir "build\reports\allure-report")
)

if (-not (Test-Path $gradlew)) {
  throw "gradlew.bat not found: $gradlew"
}

function Remove-AllureHistory {
  Write-Host "==> Cleaning Allure history..."
  foreach ($dir in @($resultsDir) + $reportDirs) {
    if (-not (Test-Path $dir)) {
      continue
    }
    try {
      Remove-Item -LiteralPath $dir -Recurse -Force -ErrorAction Stop
      Write-Host "    removed $dir"
    } catch {
      $msg = $_.Exception.Message
      Write-Host "    WARN: cannot remove $dir - $msg"
      Write-Host "    Close allureServe / report browser and retry."
      throw
    }
  }
}

function Invoke-Gradle([string[]] $GradleArgs) {
  Push-Location $repoRoot
  try {
    & $gradlew @GradleArgs
    if ($LASTEXITCODE -ne 0) {
      $joined = $GradleArgs -join " "
      throw "Gradle failed (exit $LASTEXITCODE): $joined"
    }
  } finally {
    Pop-Location
  }
}

if ($FromExisting) {
  if (-not (Test-Path $resultsDir) -or -not (Get-ChildItem $resultsDir -ErrorAction SilentlyContinue)) {
    throw "No allure-results found. Run tests first or use -Tag / -Tests / -AllTests."
  }
} elseif ($SkipTests) {
  Remove-AllureHistory
  Write-Host "==> History cleaned. Run tests, then: .\allure-report.ps1 -FromExisting"
  exit 0
} else {
  $hasTests = -not [string]::IsNullOrWhiteSpace($Tests)
  $hasTag = -not [string]::IsNullOrWhiteSpace($Tag)
  if (-not $AllTests -and -not $hasTests -and -not $hasTag) {
    throw "Specify -Tag <name>, -Tests <class>, -AllTests, -SkipTests, or -FromExisting."
  }
  Remove-AllureHistory
  Write-Host "==> Running tests..."
  $testArgs = [System.Collections.Generic.List[string]]::new()
  $testArgs.Add(":rfr-e2e:test")
  if ($hasTag) {
    $testArgs.Add("-DincludeTags=$Tag")
    Write-Host "    includeTags=$Tag"
  }
  if ($hasTests) {
    $testArgs.Add("--tests")
    $testArgs.Add($Tests)
  }
  Invoke-Gradle $testArgs.ToArray()
}

Write-Host "==> Generating Allure report..."
if ($NoServe) {
  Invoke-Gradle @(":rfr-e2e:allureReport")
  Write-Host "==> Report: $moduleDir\build\reports\allure-report\"
} else {
  Invoke-Gradle @(":rfr-e2e:allureServe")
}
