@echo off
cd /d "%~dp0\.."
if not exist out mkdir out
echo Compiling PMMR Storage Engine...
javac -d out src\pmmr\*.java
if %ERRORLEVEL% equ 0 (
    echo Compilation successful!
) else (
    echo Compilation failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
