@echo off
cd /d "%~dp0\.."
call scripts\build.bat || exit /b 1
echo Launching 100k transaction benchmark demo...
java -cp out pmmr.Main demo
