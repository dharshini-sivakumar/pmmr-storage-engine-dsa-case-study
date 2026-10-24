@echo off
cd /d "%~dp0\.."
echo Cleaning build and temporary files...
if exist out rmdir /s /q out
if exist target rmdir /s /q target
if exist build rmdir /s /q build
if exist *.pmmr del /q *.pmmr
if exist *.class del /s /q *.class
echo Clean complete.
