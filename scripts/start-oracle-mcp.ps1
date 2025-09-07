# Oracle MCP Server Startup Script (PowerShell)
# Provides convenient startup options for different configurations

param(
    [string]$Profile = "enhanced",
    [string]$Transport = "stdio",
    [string]$OracleHost = "localhost",
    [int]$OraclePort = 1521,
    [string]$OracleSid = "XE",
    [string]$OracleServiceName = "",
    [string]$OracleUsername = "hr",
    [string]$OraclePassword = "password",
    [switch]$Help
)

# Function to display usage
function Show-Usage {
    Write-Host "Oracle MCP Server Startup Script (PowerShell)" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\start-oracle-mcp.ps1 [OPTIONS]" -ForegroundColor White
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  -Profile PROFILE          Set Spring profile (enhanced|enterprise|dev|prod) [default: enhanced]"
    Write-Host "  -Transport TRANSPORT      Set MCP transport (stdio|rest) [default: stdio]"
    Write-Host "  -OracleHost HOST          Oracle host [default: localhost]"
    Write-Host "  -OraclePort PORT          Oracle port [default: 1521]"
    Write-Host "  -OracleSid SID            Oracle SID [default: XE]"
    Write-Host "  -OracleServiceName NAME   Oracle Service Name (overrides SID if specified)"
    Write-Host "  -OracleUsername USERNAME  Oracle username [default: hr]"
    Write-Host "  -OraclePassword PASSWORD  Oracle password [default: password]"
    Write-Host "  -Help                     Display this help message"
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Green
    Write-Host "  .\start-oracle-mcp.ps1                                             # Start with default enhanced profile"
    Write-Host "  .\start-oracle-mcp.ps1 -Profile enterprise                        # Start with enterprise profile (75+ tools)"
    Write-Host "  .\start-oracle-mcp.ps1 -Profile dev -Transport rest               # Start in development mode with REST API"
    Write-Host "  .\start-oracle-mcp.ps1 -OracleHost oracle-server -OracleUsername system -OraclePassword manager  # Connect to remote Oracle"
    Write-Host ""
}

# Show help if requested
if ($Help) {
    Show-Usage
    exit 0
}

# Validate profile
$ValidProfiles = @("enhanced", "enterprise", "dev", "prod", "test")
if ($ValidProfiles -notcontains $Profile) {
    Write-Host "Error: Invalid profile '$Profile'. Must be one of: $($ValidProfiles -join ', ')" -ForegroundColor Red
    exit 1
}

# Validate transport
$ValidTransports = @("stdio", "rest")
if ($ValidTransports -notcontains $Transport) {
    Write-Host "Error: Invalid transport '$Transport'. Must be one of: $($ValidTransports -join ', ')" -ForegroundColor Red
    exit 1
}

# Build Oracle JDBC URL
if ($OracleServiceName) {
    # Use service name format
    $OracleUrl = "jdbc:oracle:thin:@${OracleHost}:${OraclePort}/${OracleServiceName}"
} else {
    # Use SID format
    $OracleUrl = "jdbc:oracle:thin:@${OracleHost}:${OraclePort}:${OracleSid}"
}

# Determine JAR file
$JarFile = "target\mcp-oracledb-server-1.0.0-PRODUCTION.jar"
if (-not (Test-Path $JarFile)) {
    Write-Host "Error: JAR file not found: $JarFile" -ForegroundColor Red
    Write-Host "Please run 'mvn clean package' first" -ForegroundColor Yellow
    exit 1
}

# Display startup information
Write-Host "🚀 Starting Oracle MCP Server" -ForegroundColor Green
Write-Host "Profile: $Profile" -ForegroundColor Cyan
Write-Host "Transport: $Transport" -ForegroundColor Cyan
Write-Host "Oracle URL: $OracleUrl" -ForegroundColor Cyan
Write-Host "Username: $OracleUsername" -ForegroundColor Cyan

# Determine tool count based on profile
switch ($Profile) {
    "enhanced" { $ToolCount = "55+" }
    "enterprise" { $ToolCount = "75+" }
    default { $ToolCount = "55+" }
}

Write-Host "Tools Available: $ToolCount" -ForegroundColor Cyan
Write-Host ""

# Set environment variables
$env:ORACLE_USERNAME = $OracleUsername
$env:ORACLE_PASSWORD = $OraclePassword
$env:SPRING_DATASOURCE_URL = $OracleUrl

# Build Spring profiles
if ($Transport -eq "stdio") {
    $SpringProfiles = "$Profile,mcp"
} else {
    $SpringProfiles = $Profile
}

# Build Java command
$JavaOpts = @(
    "-XX:+UseG1GC",
    "-XX:MaxRAMPercentage=75.0",
    "-Dspring.profiles.active=$SpringProfiles"
)

if ($Transport -eq "rest") {
    $JavaOpts += "-Dserver.port=8080"
}

# Start the server
Write-Host "Starting server with profile: $SpringProfiles" -ForegroundColor Green
Write-Host ""

$JavaArgs = $JavaOpts + @("-jar", $JarFile)
& java @JavaArgs
