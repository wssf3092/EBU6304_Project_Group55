$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$artifactRoot = Join-Path $repoRoot "artifacts\qa"
$screenshotsDir = Join-Path $artifactRoot "screenshots"
$logsDir = Join-Path $artifactRoot "logs"
$resultJson = Join-Path $artifactRoot "e2e-results.json"
$reportPath = Join-Path $artifactRoot "report.md"
$serverLog = Join-Path $logsDir "server.log"
$dataRoot = Join-Path $env:TEMP "ta-recruitment-qa-run-data"
$baseUrl = "http://127.0.0.1:18080"
$port = 18080

function Remove-PathIfExists {
    param([string]$PathValue)
    if (Test-Path $PathValue) {
        Remove-Item -Path $PathValue -Recurse -Force
    }
}

function Invoke-LoggedNativeCommand {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$WorkingDirectory,
        [string]$LogPath
    )

    Set-Content -Path $LogPath -Value ""
    Push-Location $WorkingDirectory
    try {
        & $FilePath @Arguments 2>&1 | Tee-Object -FilePath $LogPath
        if ($LASTEXITCODE -ne 0) {
            throw "Command failed: $FilePath $($Arguments -join ' ')"
        }
    }
    finally {
        Pop-Location
    }
}

function Wait-ForHttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds,
        [System.Diagnostics.Process]$Process,
        [string]$LogPath
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if ($Process.HasExited) {
            if (Test-Path $LogPath) {
                Get-Content -Path $LogPath -Tail 60
            }
            throw "Server exited before becoming ready."
        }

        try {
            $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                return
            }
        } catch {
        }

        Start-Sleep -Seconds 2
    }

    if (Test-Path $LogPath) {
        Get-Content -Path $LogPath -Tail 60
    }
    throw "Server did not become ready at $Url within $TimeoutSeconds seconds."
}

function Stop-ProcessTree {
    param([int]$ProcessId)

    if ($ProcessId -le 0) {
        return
    }

    $target = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if ($null -eq $target) {
        return
    }

    & taskkill /PID $ProcessId /T /F | Out-Null
}

function Stop-StaleQaProcesses {
    $staleProcesses = Get-CimInstance Win32_Process | Where-Object {
        ($_.CommandLine -like '*com.group55.ta.DevServer*') -or
        ($_.CommandLine -like '*artifacts\qa\logs\server.log*')
    }

    foreach ($process in $staleProcesses) {
        Stop-ProcessTree -ProcessId $process.ProcessId
    }

    $portOwners = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        Where-Object { $_ -gt 0 }

    foreach ($processId in $portOwners) {
        Stop-ProcessTree -ProcessId $processId
    }
}

Stop-StaleQaProcesses

Remove-PathIfExists -PathValue $screenshotsDir
Remove-PathIfExists -PathValue $logsDir
Remove-PathIfExists -PathValue $resultJson
Remove-PathIfExists -PathValue $reportPath
Remove-PathIfExists -PathValue $dataRoot

New-Item -ItemType Directory -Path $screenshotsDir -Force | Out-Null
New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
New-Item -ItemType Directory -Path $dataRoot -Force | Out-Null

$unitLog = Join-Path $logsDir "mvn-test.log"
$packageLog = Join-Path $logsDir "mvn-package.log"
$e2eLog = Join-Path $logsDir "e2e.log"
$nodeVersion = (node -v).Trim()
$mavenVersion = ((mvn -v) | Select-Object -First 1).Trim()
$javaVersion = ((mvn -v) | Select-Object -Skip 2 -First 1).Trim()

Write-Host "[QA] Running Maven unit tests..."
Invoke-LoggedNativeCommand -FilePath "mvn" -Arguments @("-q", "test") -WorkingDirectory $repoRoot -LogPath $unitLog

$serverProcess = $null
try {
    Write-Host "[QA] Starting embedded Tomcat on $baseUrl ..."
    $serverCommand = "set ""JAVA_TOOL_OPTIONS=-Dta.data.root=$dataRoot"" && mvn -q -DskipTests compile exec:java ""-Dexec.mainClass=com.group55.ta.DevServer"" ""-Dexec.args=$port"" > ""$serverLog"" 2>&1"
    $serverProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $serverCommand -WorkingDirectory $repoRoot -PassThru -WindowStyle Hidden

    Wait-ForHttpReady -Url "$baseUrl/auth/login" -TimeoutSeconds 120 -Process $serverProcess -LogPath $serverLog

    Write-Host "[QA] Running Playwright browser regression..."
    $env:QA_BASE_URL = $baseUrl
    $env:QA_DATA_ROOT = $dataRoot
    Invoke-LoggedNativeCommand -FilePath "node" -Arguments @((Join-Path $repoRoot "scripts\qa-e2e.mjs")) -WorkingDirectory $repoRoot -LogPath $e2eLog
}
finally {
    Remove-Item Env:\QA_BASE_URL -ErrorAction SilentlyContinue
    Remove-Item Env:\QA_DATA_ROOT -ErrorAction SilentlyContinue
    if ($null -ne $serverProcess) {
        Write-Host "[QA] Stopping embedded Tomcat..."
        Stop-ProcessTree -ProcessId $serverProcess.Id
    }
}

Write-Host "[QA] Running WAR packaging check..."
Invoke-LoggedNativeCommand -FilePath "mvn" -Arguments @("-q", "-DskipTests", "package") -WorkingDirectory $repoRoot -LogPath $packageLog

$e2e = Get-Content -Path $resultJson -Raw | ConvertFrom-Json
$generatedAt = Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz"

$scenarioBlocks = foreach ($scenario in @($e2e.scenarios)) {
    $lines = @("- ``$($scenario.status)`` $($scenario.name)")
    if ($scenario.screenshot) {
        $lines += "  Screenshot: ``$($scenario.screenshot)``"
    }
    if ($scenario.details) {
        $lines += "  Evidence: ``$($scenario.details)``"
    }
    $lines -join "`n"
}

$persistenceBlocks = foreach ($check in @($e2e.persistenceChecks)) {
    "- ``$($check.status)`` $($check.name)`n  Detail: ``$($check.detail)``"
}

$reportLines = @(
    "# TA Recruitment System QA Report",
    "",
    "## Feature",
    'End-to-end TA recruitment workflow across `TA`, `MO`, and `Admin` roles with deterministic screenshots and isolated persistence.',
    "",
    "## Environment",
    "- Generated at: ``$generatedAt``",
    '- OS: `Windows 11`',
    "- Java: ``$javaVersion``",
    "- Maven: ``$mavenVersion``",
    "- Node: ``$nodeVersion``",
    "- Browser: ``$($e2e.browser)``",
    "- Local URL: ``$baseUrl``",
    "- Data root: ``$dataRoot``",
    '- Unit test command: `mvn -q test`',
    "- Startup command: ``mvn -q -DskipTests compile exec:java ""-Dexec.mainClass=com.group55.ta.DevServer"" ""-Dexec.args=$port""``",
    '- Browser command: `node scripts/qa-e2e.mjs`',
    '- Package command: `mvn -q -DskipTests package`',
    "",
    "## Scenarios Executed"
)

$reportLines += $scenarioBlocks
$reportLines += @(
    '- `PASS` Core service tests pass.',
    '  Evidence: `mvn -q test`',
    '- `PASS` WAR packaging succeeds.',
    '  Evidence: `mvn -q -DskipTests package`',
    "",
    "## API and Persistence Checks"
)
$reportLines += $persistenceBlocks
$reportLines += @(
    "",
    "## Defects Found In First Pass",
    "- None during this run.",
    "",
    "## Fixes Applied",
    '- Added reproducible QA automation scripts at `scripts/run-qa.ps1` and `scripts/qa-e2e.mjs` for future reruns.',
    "",
    "## Regression Result",
    '- Final result: `PASS`',
    '- Startup log: `artifacts/qa/logs/server.log`',
    '- E2E log: `artifacts/qa/logs/e2e.log`',
    '- Screenshots directory: `artifacts/qa/screenshots/`'
)

$reportLines | Set-Content -Path $reportPath -Encoding UTF8
Write-Host "[QA] Report written to $reportPath"
