@echo off
REM
REM BIBO-BibTeX VocBench Plugin Installer for Windows
REM
REM Usage: install.bat [VOCBENCH_HOME]
REM

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PLUGIN_JAR_PATTERN=vocbench-plugin\target\vocbench-plugin-*-all.jar"

echo.
echo ==========================================
echo  BIBO-BibTeX VocBench Plugin Installer
echo ==========================================
echo.

REM Find the plugin JAR
set "PLUGIN_JAR="
for %%f in ("%SCRIPT_DIR%%PLUGIN_JAR_PATTERN%") do (
    set "PLUGIN_JAR=%%f"
)

if not defined PLUGIN_JAR (
    echo [ERROR] Plugin JAR not found. Please build the project first:
    echo     mvn clean package -DskipTests
    exit /b 1
)

echo [INFO] Found plugin JAR: %PLUGIN_JAR%

REM Determine VocBench home
if not "%~1"=="" (
    set "VOCBENCH_HOME=%~1"
) else if defined VOCBENCH_HOME (
    REM Use environment variable
) else (
    REM Check common paths
    if exist "C:\VocBench" set "VOCBENCH_HOME=C:\VocBench"
    if exist "C:\Program Files\VocBench" set "VOCBENCH_HOME=C:\Program Files\VocBench"
    if exist "%USERPROFILE%\VocBench" set "VOCBENCH_HOME=%USERPROFILE%\VocBench"
)

if not defined VOCBENCH_HOME (
    echo [WARN] VocBench installation not found automatically.
    set /p VOCBENCH_HOME="Enter VocBench installation directory: "
)

if not exist "%VOCBENCH_HOME%" (
    echo [ERROR] Directory does not exist: %VOCBENCH_HOME%
    exit /b 1
)

echo [INFO] VocBench home: %VOCBENCH_HOME%

REM Create plugins directory
set "PLUGINS_DIR=%VOCBENCH_HOME%\plugins"
if not exist "%PLUGINS_DIR%" (
    echo [INFO] Creating plugins directory...
    mkdir "%PLUGINS_DIR%"
)

echo [INFO] Plugins directory: %PLUGINS_DIR%

REM Install the plugin
echo [INFO] Installing plugin...
for %%f in ("%PLUGIN_JAR%") do set "JAR_NAME=%%~nxf"
copy /Y "%PLUGIN_JAR%" "%PLUGINS_DIR%\%JAR_NAME%" > nul

echo [INFO] Plugin installed: %PLUGINS_DIR%\%JAR_NAME%

REM Create configuration
set "CONFIG_DIR=%VOCBENCH_HOME%\config"
set "CONFIG_FILE=%CONFIG_DIR%\bibo-bibtex.properties"

if not exist "%CONFIG_DIR%" mkdir "%CONFIG_DIR%"

if exist "%CONFIG_FILE%" (
    echo [WARN] Configuration file already exists: %CONFIG_FILE%
    set /p OVERWRITE="Overwrite? [y/N] "
    if /i not "!OVERWRITE!"=="y" (
        echo [INFO] Keeping existing configuration.
        goto :done
    )
)

(
echo # BIBO-BibTeX Converter Plugin Configuration
echo #
echo # Repository settings
echo repository.type=native
echo repository.dataDir=${vocbench.data}/bibo-bibtex
echo.
echo # Namespace settings
echo namespace.prefix=http://example.org/bibo/
echo.
echo # Field mapping
echo field.mapping.strict=false
echo.
echo # Identifier generation strategy: author-year, author-title, hash
echo identifier.strategy=author-year
echo.
echo # Duplicate detection
echo duplicate.detection.enabled=true
echo duplicate.similarity.threshold=0.85
) > "%CONFIG_FILE%"

echo [INFO] Configuration created: %CONFIG_FILE%

:done
echo.
echo [INFO] Installation complete!
echo.
echo Next steps:
echo   1. Restart VocBench if it's running
echo   2. The plugin will appear in the plugin list
echo   3. Configure settings in: %VOCBENCH_HOME%\config\bibo-bibtex.properties
echo.

endlocal
