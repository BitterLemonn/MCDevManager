@echo off
setlocal

set "PROJECT_DIR=%~dp0.."
set "MCP_EXE=%PROJECT_DIR%\mcpServer\build\install\mcdev-mcp\bin\mcdev-mcp.bat"

if not exist "%MCP_EXE%" (
    call "%PROJECT_DIR%\gradlew.bat" -q :mcpServer:installDist 1>&2
    if errorlevel 1 exit /b %errorlevel%
)

call "%MCP_EXE%"
