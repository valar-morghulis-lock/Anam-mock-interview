<#
.SYNOPSIS
    End-to-end test script for the Mock Interview Coach backend.
    Creates a session, seeds fake candidate answers for every question,
    generates the STAR feedback report, and downloads it as a PDF.

.USAGE
    .\test-report-flow.ps1
    .\test-report-flow.ps1 -BaseUrl "http://localhost:8080" -OutFileName "my-report.pdf"
    .\test-report-flow.ps1 -Role "Staff Engineer" -Seniority "STAFF" -PersonaStyle "BAR_RAISER"
#>

param(
    [string]$BaseUrl = "http://localhost:8080/api/interviews",
    [string]$OutFileName = "report.pdf",
    [string]$Role = "Backend Engineer",
    [string]$Seniority = "SENIOR",
    [string]$PersonaStyle = "SUPPORTIVE",
    [string[]]$Competencies = @("conflict", "failure"),
    [int]$QuestionsPerCompetency = 2,
    [int]$TimeLimitSec = 120
)

$ErrorActionPreference = "Stop"

# Always resolves relative to this script's own location, not the caller's
# current directory — so output always lands in the same place regardless
# of where the script is invoked from.
$outputDir = Join-Path $PSScriptRoot "output"
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}
$outFile = Join-Path $outputDir $OutFileName

# A pool of fake candidate answers — cycled through if there are more
# questions than answers, so this works regardless of question count.
$fakeAnswers = @(
    "We had a conflict on my team where two engineers disagreed on architecture. I facilitated a meeting, we evaluated both approaches against our scaling needs, and picked a hybrid solution. It reduced our deploy time by 40 percent.",
    "I once pushed a change that broke our staging environment for a full day. I immediately rolled it back, wrote a postmortem, and added a pre-deploy checklist that we still use today.",
    "A project I led failed to hit its deadline because we underestimated the migration complexity. I renegotiated scope with stakeholders, split the work into phases, and delivered the core functionality two weeks late instead of missing entirely.",
    "I disagreed with my manager's decision to skip code review for a hotfix. I raised my concern directly, we agreed on a lightweight review instead of skipping it entirely, and it caught a real bug before release."
)

Write-Host "1. Creating interview session..." -ForegroundColor Cyan
$body = @{
    role                   = $Role
    seniority              = $Seniority
    personaStyle           = $PersonaStyle
    competencyNames        = $Competencies
    questionsPerCompetency = $QuestionsPerCompetency
    timeLimitSec           = $TimeLimitSec
} | ConvertTo-Json

$session = Invoke-RestMethod -Uri $BaseUrl -Method Post -Body $body -ContentType "application/json"
Write-Host "   Session created: $($session.sessionId)" -ForegroundColor Green
Write-Host "   $($session.questions.Count) questions assigned" -ForegroundColor Green

Write-Host "2. Seeding a candidate answer for each question..." -ForegroundColor Cyan
for ($i = 0; $i -lt $session.questions.Count; $i++) {
    $question = $session.questions[$i]
    $answer = $fakeAnswers[$i % $fakeAnswers.Count]

    $transcriptBody = @{
        sessionQuestionId = $question.sessionQuestionId
        speaker           = "CANDIDATE"
        content           = $answer
    } | ConvertTo-Json

    Invoke-RestMethod -Uri "$BaseUrl/$($session.sessionId)/transcript" `
        -Method Post -Body $transcriptBody -ContentType "application/json" | Out-Null

    Write-Host "   [$($i + 1)/$($session.questions.Count)] $($question.text)" -ForegroundColor DarkGray
}

Write-Host "3. Generating feedback report (Groq scoring)..." -ForegroundColor Cyan
$report = Invoke-RestMethod -Uri "$BaseUrl/$($session.sessionId)/report" -Method Post
Write-Host "   Report generated: $($report.reportId)" -ForegroundColor Green
Write-Host "   ---" -ForegroundColor DarkGray
Write-Host "   $($report.overallStrengths)" -ForegroundColor White
Write-Host "   ---" -ForegroundColor DarkGray

Write-Host "4. Downloading PDF..." -ForegroundColor Cyan
Invoke-WebRequest -Uri "$BaseUrl/$($session.sessionId)/report/pdf" -Method Get -OutFile $outFile
Write-Host "   Saved to $outFile" -ForegroundColor Green

Write-Host ""
Write-Host "Done. Session ID: $($session.sessionId)" -ForegroundColor Yellow