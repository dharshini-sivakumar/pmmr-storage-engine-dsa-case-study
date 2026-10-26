@echo off
if not exist out mkdir out
javac -d out src\pmmr\*.java || exit /b 1
if "%1"=="test" (java -cp out pmmr.Tests) else if "%1"=="demo" (java -cp out pmmr.Main demo) else (java -cp out pmmr.Main)
