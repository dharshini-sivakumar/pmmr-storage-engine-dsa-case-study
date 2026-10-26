@echo off
cd /d "%~dp0\.."
call scripts\build.bat || exit /b 1
echo Running full PMMR test suite...
java -cp out pmmr.Tests
